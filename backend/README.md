# CoopShield SOC — Backend

> Projeto independente, educacional e de portfólio. Todos os dados, usuários, eventos,
> transações e incidentes processados são sintéticos.

Backend Java 21 / Spring Boot do CoopShield SOC, estruturado como monólito modular
orientado a eventos (ver [docs/architecture/overview.md](../docs/architecture/overview.md)
e [ADR-001](../docs/adr/ADR-001-monolito-modular.md)/[ADR-009](../docs/adr/ADR-009-arquitetura-hexagonal.md)).

## Estrutura do Reactor Maven

```
backend/
├── pom.xml              (parent — gerencia versões e módulos)
├── sharedkernel/        (tipos e contratos comuns: EventEnvelope, EventId, CorrelationId)
├── identity/
├── accesscontrol/
├── eventingestion/
├── eventnormalization/
├── dataprotection/
├── detection/
├── risk/
├── alert/
├── incident/
├── playbook/
├── audit/
├── observability/
├── simulation/
└── app/                 (aplicação Spring Boot executável — ponto de entrada)
```

Cada módulo de domínio segue arquitetura hexagonal internamente
(`domain` / `application` / `infrastructure`). Além de `sharedkernel`, os módulos
`identity`, `accesscontrol` e `audit` já possuem lógica real (autenticação, RBAC,
persistência em MongoDB e trilha de auditoria — ver seções "Autenticação e Autorização"
e "Persistência" abaixo); os demais módulos ainda têm apenas o esqueleto de pacotes
documentado, pronto para receber a implementação funcional das fases seguintes (ver
[docs/roadmap.md](../docs/roadmap.md)).

## Pré-requisitos

- Java 21 (testado com Eclipse Temurin 21.0.12).
- Maven 3.9+ (ou utilize o wrapper, quando disponível).
- Docker (necessário para o MongoDB local via Docker Compose e para os testes de
  integração com Testcontainers — ver [ADR-012](../docs/adr/ADR-012-mongodb-real-fase-3.md)).

## Executar Localmente

O backend precisa de um MongoDB acessível para subir (o seeder de usuários sintéticos
grava no banco na inicialização). Suba apenas o MongoDB do Docker Compose antes do
backend:

```bash
cd infrastructure
cp .env.example .env   # apenas na primeira vez
docker compose up -d mongodb

cd ../backend
mvn spring-boot:run -pl app -am -Dspring-boot.run.profiles=local
```

O perfil `local` aponta, por padrão, para `mongodb://localhost:27017` com as
credenciais sintéticas de `infrastructure/.env.example` (ver
`application-local.yml`); sobrescreva com `SPRING_DATA_MONGODB_URI` se necessário.

A aplicação sobe em `http://localhost:8080`, com:

- Health check: `GET /actuator/health`
- Documentação OpenAPI: `GET /v3/api-docs`
- Swagger UI: `GET /swagger-ui.html`
- Autenticação: `POST /api/v1/auth/login`, `/refresh`, `/logout` (ver abaixo)

## Autenticação e Autorização (Fase 2)

O backend expõe autenticação por JWT de curta duração + refresh token opaco
rotacionado a cada uso, com bloqueio temporário após tentativas repetidas e
autorização por perfil (RBAC), conforme
[docs/architecture/roles-permissions.md](../docs/architecture/roles-permissions.md).
A persistência de usuários, refresh tokens e auditoria é real em MongoDB desde a
Fase 3 (ver seção "Persistência" abaixo e [ADR-012](../docs/adr/ADR-012-mongodb-real-fase-3.md));
o desenho original de autenticação/RBAC foi provado com adaptadores em memória na
Fase 2 ([ADR-011](../docs/adr/ADR-011-persistencia-em-memoria-fase-2.md)), sem alterar
domínio ou casos de uso ao trocar o adaptador.

### Usuários sintéticos (semeados na inicialização)

Um usuário sintético por perfil é criado automaticamente ao subir a aplicação, todos
com a mesma senha sintética `Synthetic#Pass123` (apenas para uso local/demonstrativo —
nunca representa uma credencial real):

`synthetic-soc-analyst-01`, `synthetic-soc-manager-01`, `synthetic-employee-01`,
`synthetic-branch-manager-01`, `synthetic-it-admin-01`, `synthetic-auditor-01`.

### Fluxo de exemplo (curl)

```bash
# Login
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"synthetic-soc-analyst-01","password":"Synthetic#Pass123"}'

# Consultar a própria identidade (necessita do accessToken retornado acima)
curl -s http://localhost:8080/api/v1/me -H "Authorization: Bearer <accessToken>"

# Renovar (rotaciona o refresh token; o antigo deixa de ser valido)
curl -s -X POST http://localhost:8080/api/v1/auth/refresh \
  -H "Content-Type: application/json" -d '{"refreshToken":"<refreshToken>"}'

# Logout (revoga o refresh token)
curl -s -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Content-Type: application/json" -d '{"refreshToken":"<refreshToken>"}'
```

### Regras de autorização por rota

| Prefixo | Perfis permitidos |
|---------|--------------------|
| `/api/v1/auth/**`, `/actuator/health`, `/v3/api-docs/**`, `/swagger-ui/**` | Público |
| `/api/v1/admin/**` | `IT_ADMIN` |
| `/api/v1/audit/**` | `AUDITOR` |
| `/api/v1/soc/**` | `SOC_ANALYST`, `SOC_MANAGER` |
| qualquer outra rota | Qualquer usuário autenticado |

Os prefixos `/api/v1/admin`, `/api/v1/audit` e `/api/v1/soc` ainda não têm
controladores de negócio (chegam nas Fases 6 a 9); a regra de autorização já está em
vigor e é validada nos testes de integração pela distinção entre 403 (bloqueado pelo
perfil) e 404 (autorizado, mas sem endpoint ainda).

### Segurança das credenciais

- Falha de credenciais e conta bloqueada retornam a **mesma resposta HTTP genérica**
  (401, `invalid_credentials`), para não permitir enumeração de usuários nem revelar
  o estado de bloqueio a um observador externo. A distinção fica registrada apenas na
  auditoria interna (`AUTHENTICATION_FAILURE` vs. `ACCOUNT_LOCKED`).
- Senhas são armazenadas com BCrypt; refresh tokens são armazenados como hash SHA-256
  do segredo (nunca em texto puro), conforme
  [ADR-004](../docs/adr/ADR-004-tokenizacao.md).
- Nenhuma senha, hash ou token é registrado em log.

### Propriedades de configuração

| Propriedade | Padrão | Descrição |
|---|---|---|
| `coopshield.security.jwt.secret` | chave sintética local | Chave HS256 (sobrescrever via variável de ambiente fora do uso local) |
| `coopshield.security.jwt.access-token-ttl` | `PT15M` | Duração do access token |
| `coopshield.security.lockout.max-failed-attempts` | `5` | Tentativas antes do bloqueio |
| `coopshield.security.lockout.lockout-duration` | `PT15M` | Duração do bloqueio temporário |
| `coopshield.security.lockout.refresh-token-ttl` | `P7D` | Duração do refresh token |

## Persistência (Fase 3)

Os módulos `identity` e `audit` persistem em MongoDB real através de adaptadores que
implementam as portas de aplicação já existentes desde a Fase 2, sem alterar domínio ou
casos de uso (ver [ADR-012](../docs/adr/ADR-012-mongodb-real-fase-3.md)):

| Coleção | Módulo | Índices |
|---|---|---|
| `users` | `identity` | `username` (único) |
| `refresh_tokens` | `identity` | `userId`; `expiresAt` (TTL — expurgo automático de tokens expirados) |
| `audit_logs` | `audit` | `eventType`, `actor`, `timestamp` (sem TTL — retenção indefinida no MVP) |

As demais coleções previstas no produto completo (`security_events`, `alerts`,
`incidents`, `detection_rules`, `playbooks`, `protected_data`, `user_baselines`,
`devices`, `simulation_runs`) serão criadas nas fases em que seus módulos de domínio
forem implementados (ver tabela de fases em ADR-012), reaplicando o mesmo padrão de
adaptador.

## Build e Testes

```bash
cd backend
mvn verify
```

Isso compila todos os 16 módulos (14 de domínio + `sharedkernel` + `app`) e executa os
testes JUnit 5/Mockito/AssertJ de cada módulo: domínio, aplicação, infraestrutura, os
testes de persistência MongoDB via Testcontainers (`identity`, `audit`) e os testes de
integração de autenticação/RBAC no módulo `app` (também via Testcontainers, ver
`AbstractIntegrationTest`). **Docker precisa estar em execução** para os testes de
Testcontainers; sem Docker disponível, use `mvn verify -Dtest='!*MongoUserRepositoryTest,!*MongoAuditLogTest,!*MongoRefreshTokenRepositoryTest' -DfailIfNoTests=false -pl '!app'`
para rodar apenas os testes que não dependem de um MongoDB real.

O build empacota o artefato executável em `app/target/coopshield-soc.jar`.

## Perfis

| Perfil | Uso |
|--------|-----|
| `local` | Execução local fora de container, logs em nível DEBUG para o pacote do projeto, MongoDB em `localhost:27017` |
| `docker` | Execução via Docker Compose, logs em nível INFO, MongoDB via `SPRING_DATA_MONGODB_URI` injetada pelo Compose |

Perfis/configuração adicionais para Kafka serão introduzidos na Fase 4, conforme o
[Roadmap](../docs/roadmap.md).

## Imagem Docker

```bash
docker build -t coopshield-soc-backend -f backend/Dockerfile backend
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=docker coopshield-soc-backend
```

A imagem usa build multi-stage (Maven + JDK 21 para build, JRE 21 para runtime), roda
com usuário não privilegiado e expõe um `HEALTHCHECK` sobre `/actuator/health`.

## Limitações desta Fase

- Sem endpoints de negócio de domínio ainda (eventos, alertas, incidentes) — chegam
  nas Fases 4 a 9. Autenticação e RBAC já são reais desde a Fase 2 (ver acima).
- Sem Kafka configurado no backend ainda — chega na Fase 4. MongoDB (usuários, refresh
  tokens, auditoria) já é real desde a Fase 3 (ver [ADR-012](../docs/adr/ADR-012-mongodb-real-fase-3.md)).
- Sem política de arquivamento/expurgo formal para `audit_logs` além do TTL nativo já
  aplicado a `refresh_tokens` — retenção é indefinida no MVP (ver
  [Riscos Técnicos](../docs/architecture/technical-risks.md)).
