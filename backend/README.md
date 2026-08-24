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
`identity`, `accesscontrol`, `audit`, `eventingestion`, `eventnormalization`,
`simulation` e `detection` já possuem lógica real (autenticação, RBAC, persistência em
MongoDB, trilha de auditoria, o pipeline de ingestão/normalização de eventos, o
laboratório de simulação e o motor de detecção — ver seções "Autenticação e
Autorização", "Persistência", "Ingestão e Normalização de Eventos", "Laboratório de
Simulação" e "Motor de Detecção" abaixo); os demais módulos ainda têm apenas o
esqueleto de pacotes documentado, pronto para receber a implementação funcional das
fases seguintes (ver [docs/roadmap.md](../docs/roadmap.md)).

## Pré-requisitos

- Java 21 (testado com Eclipse Temurin 21.0.12).
- Maven 3.9+ (ou utilize o wrapper, quando disponível).
- Docker (necessário para o MongoDB/Kafka locais via Docker Compose e para os testes de
  integração com Testcontainers — ver [ADR-012](../docs/adr/ADR-012-mongodb-real-fase-3.md)
  e [ADR-013](../docs/adr/ADR-013-pipeline-ingestao-normalizacao.md)).

## Executar Localmente

O backend precisa de um MongoDB e um Kafka acessíveis para subir (o seeder de usuários
sintéticos grava no banco na inicialização, e o pipeline de eventos depende de Kafka
estar no ar). Suba MongoDB e Kafka do Docker Compose antes do backend:

```bash
cd infrastructure
cp .env.example .env   # apenas na primeira vez
docker compose up -d mongodb kafka

cd ../backend
mvn spring-boot:run -pl app -am -Dspring-boot.run.profiles=local
```

O perfil `local` aponta, por padrão, para `mongodb://localhost:27017` e
`localhost:9092` (Kafka), com as credenciais sintéticas de
`infrastructure/.env.example` (ver `application-local.yml`); sobrescreva com
`SPRING_DATA_MONGODB_URI`/`SPRING_KAFKA_BOOTSTRAP_SERVERS` se necessário.

A aplicação sobe em `http://localhost:8080`, com:

- Health check: `GET /actuator/health`
- Documentação OpenAPI: `GET /v3/api-docs`
- Swagger UI: `GET /swagger-ui.html`
- Autenticação: `POST /api/v1/auth/login`, `/refresh`, `/logout` (ver abaixo)
- Ingestão de eventos: `POST /api/v1/events` (autenticado — ver "Ingestão e
  Normalização de Eventos" abaixo)

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

As demais coleções previstas no produto completo (`alerts`, `incidents`,
`detection_rules`, `playbooks`, `protected_data`, `user_baselines`, `devices`) serão
criadas nas fases em que seus módulos de domínio forem implementados (ver tabela de
fases em ADR-012), reaplicando o mesmo padrão de adaptador. `security_events`
(Fase 4) e `simulation_runs` (Fase 5) já existem — ver seções abaixo.

## Ingestão e Normalização de Eventos (Fase 4)

`eventingestion` recebe eventos sintéticos brutos, valida os campos obrigatórios e
publica em `security.raw-events`. `eventnormalization` consome esse tópico, transforma
o IP de origem em hash SHA-256 (nunca fica em texto puro em nenhum tópico/coleção a
partir daí), atribui uma classificação de dados provisória (`INTERNAL`, até o módulo
`dataprotection` da Fase 9 assumir essa responsabilidade), persiste de forma idempotente
na coleção `security_events` e publica em `security.normalized-events`. Mensagens que
falham a normalização (campo obrigatório ausente, JSON inválido) vão para
`security.dead-letter`. Ver [ADR-013](../docs/adr/ADR-013-pipeline-ingestao-normalizacao.md)
para o desenho completo.

### Fluxo de exemplo (curl)

```bash
curl -s -X POST http://localhost:8080/api/v1/events \
  -H "Content-Type: application/json" -H "Authorization: Bearer <accessToken>" \
  -d '{
    "eventType": "authentication.login.failure",
    "source": "identity-service",
    "actorUserId": "synthetic-user-01",
    "actorRole": "EMPLOYEE",
    "targetResourceType": "account",
    "targetResourceId": "synthetic-account-01",
    "action": "LOGIN",
    "outcome": "FAILURE",
    "deviceId": "synthetic-device-01",
    "deviceKnown": false,
    "sourceIp": "203.0.113.10"
  }'
# => 202 Accepted, corpo: {"eventId": "...", "correlationId": "..."}
```

`eventId`, `eventVersion`, `timestamp` e `correlationId` são opcionais — valores padrão
são gerados quando ausentes. Campos obrigatórios ausentes retornam `400 Bad Request`
com a lista de violações.

## Laboratório de Simulação (Fase 5)

O módulo `simulation` gera tráfego sintético realista para os outros módulos:
seis personagens fixos (um por perfil, ver
[docs/product/personas-use-cases.md](../docs/product/personas-use-cases.md)) e doze
cenários (`normal`, `account-compromised`, `privilege-abuse`, `mass-query`,
`atypical-export`, `pii-exposed`, `admin-change`, `api-anomaly`, `atypical-auth`,
`unknown-device`, `failures-then-success`, `unauthorized-endpoint`). Uma execução
publica os eventos gerados através do **mesmo** `EventIngestionService` usado por
`POST /api/v1/events` — não existe um caminho separado ou mais permissivo para eventos
sintéticos — e todos os eventos de uma execução compartilham um `correlationId`,
permitindo reconstruir a jornada completa em `security_events` depois da normalização.
Cada execução é registrada na coleção `simulation_runs` (reservada para esta fase em
[ADR-012](../docs/adr/ADR-012-mongodb-real-fase-3.md)).

### Fluxo de exemplo (curl)

```bash
# Listar personagens e cenários disponiveis
curl -s http://localhost:8080/api/v1/simulation/characters -H "Authorization: Bearer <accessToken>"
curl -s http://localhost:8080/api/v1/simulation/scenarios -H "Authorization: Bearer <accessToken>"

# Executar o cenario de referencia da Fase 0 ("conta possivelmente comprometida")
curl -s -X POST http://localhost:8080/api/v1/simulation/runs \
  -H "Content-Type: application/json" -H "Authorization: Bearer <accessToken>" \
  -d '{"scenarioId":"account-compromised","characterId":"roberto-nogueira","eventCount":6}'
# => 202 Accepted, corpo: {"runId":"...","correlationId":"...","status":"COMPLETED",...}

# Consultar uma execucao pelo id
curl -s http://localhost:8080/api/v1/simulation/runs/<runId> -H "Authorization: Bearer <accessToken>"
```

`eventCount` é opcional (usa o padrão do cenário quando ausente) e aceito entre 1 e 100.
Qualquer usuário autenticado pode executar o laboratório, em qualquer perfil (ver
[docs/architecture/roles-permissions.md](../docs/architecture/roles-permissions.md)).
A seleção visual de personagem/cenário, velocidade e modo passo a passo chegam com o
Laboratory do frontend (Fase 10); esta fase entrega a capacidade de geração em lote no
backend.

## Motor de Detecção (Fase 6)

`detection` consome `security.normalized-events`, avalia cada evento contra as 15
regras iniciais (carregadas de YAML em `src/main/resources/detection-rules/`, ver
[docs/detection-rules/catalog.md](../docs/detection-rules/catalog.md) e
[ADR-014](../docs/adr/ADR-014-motor-de-deteccao.md)) e, quando uma regra é acionada,
persiste a correspondência explicável em `detection_matches` e publica em
`security.detection-alerts`. Sete tipos de avaliador (contagem por limite, falhas
seguidas de sucesso, sinalização de evento único, horário atípico, viagem impossível,
múltiplas contas no mesmo dispositivo, sequência de dois tipos de evento) cobrem as 15
regras; um histórico deslizante em memória, por ator e por dispositivo, alimenta as
regras que dependem de agregação.

`riskScore` de cada correspondência usa `baseRiskScore` da regra como valor
**provisório** até o motor de risco (Fase 7) calcular a pontuação final explicável.

### Fluxo de exemplo (curl)

```bash
curl -s http://localhost:8080/api/v1/detection/rules -H "Authorization: Bearer <accessToken>"
curl -s http://localhost:8080/api/v1/detection/matches -H "Authorization: Bearer <accessToken>"
curl -s "http://localhost:8080/api/v1/detection/matches?correlationId=<correlationId>" \
  -H "Authorization: Bearer <accessToken>"
```

Executar o cenário `account-compromised` do laboratório de simulação (ver seção acima)
aciona RULE-001 (múltiplas falhas seguidas de sucesso) e RULE-003 (dispositivo
desconhecido) de ponta a ponta — validado em
`DetectionEndToEndIntegrationTest` (módulo `app`).

## Build e Testes

```bash
cd backend
mvn verify
```

Isso compila todos os 16 módulos (14 de domínio + `sharedkernel` + `app`) e executa os
testes JUnit 5/Mockito/AssertJ de cada módulo: domínio, aplicação, infraestrutura, os
testes de persistência MongoDB via Testcontainers (`identity`, `audit`,
`eventnormalization`, `simulation`, `detection`), os testes do pipeline de eventos via
Kafka+MongoDB reais (`eventingestion`, `eventnormalization`, `simulation`, `detection`)
e os testes de integração de autenticação/RBAC/ingestão/simulação/detecção no módulo
`app` (também via Testcontainers, ver `AbstractIntegrationTest`). **Docker precisa
estar em execução** para os testes de Testcontainers; sem Docker disponível, use
`mvn verify -Dtest='!*MongoUserRepositoryTest,!*MongoAuditLogTest,!*MongoRefreshTokenRepositoryTest,!*IntegrationTest' -DfailIfNoTests=false -pl '!app'`
para rodar apenas os testes que não dependem de MongoDB/Kafka reais.

O build empacota o artefato executável em `app/target/coopshield-soc.jar`.

## Perfis

| Perfil | Uso |
|--------|-----|
| `local` | Execução local fora de container, logs em nível DEBUG para o pacote do projeto, MongoDB em `localhost:27017`, Kafka em `localhost:9092` |
| `docker` | Execução via Docker Compose, logs em nível INFO, MongoDB/Kafka via `SPRING_DATA_MONGODB_URI`/`SPRING_KAFKA_BOOTSTRAP_SERVERS` injetadas pelo Compose |

## Imagem Docker

```bash
docker build -t coopshield-soc-backend -f backend/Dockerfile backend
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=docker coopshield-soc-backend
```

A imagem usa build multi-stage (Maven + JDK 21 para build, JRE 21 para runtime), roda
com usuário não privilegiado e expõe um `HEALTHCHECK` sobre `/actuator/health`.

## Limitações desta Fase

- Sem risco, alertas, incidentes, playbooks ou proteção de dados completa ainda —
  chegam nas Fases 7 a 9. Autenticação/RBAC (Fase 2), persistência MongoDB (Fase 3), o
  pipeline de ingestão/normalização de eventos via Kafka (Fase 4), o laboratório de
  simulação (Fase 5) e o motor de detecção (Fase 6) já são reais (ver acima).
- O laboratório publica em lote, de forma síncrona; controle visual de velocidade e
  execução passo a passo são responsabilidade do Laboratory do frontend (Fase 10),
  construído sobre esta capacidade de backend.
- `riskScore` de uma correspondência de regra é o `baseRiskScore` da própria regra, não
  um cálculo combinando severidade/dispositivo/volume/reincidência — isso é
  responsabilidade do motor de risco (Fase 7) — ver
  [ADR-014](../docs/adr/ADR-014-motor-de-deteccao.md).
- O histórico de agregação do motor de detecção é em memória, de uma única instância do
  backend — perdido a reinício do processo, não distribuído entre réplicas (aceitável
  para o MVP de instância única, ver ADR-014).
- `dataClassification` dos eventos normalizados é um valor provisório (`INTERNAL`) até
  o módulo `dataprotection` (Fase 9) assumir essa responsabilidade de verdade — ver
  [ADR-013](../docs/adr/ADR-013-pipeline-ingestao-normalizacao.md).
- Sem política de arquivamento/expurgo formal para `audit_logs` além do TTL nativo já
  aplicado a `refresh_tokens` — retenção é indefinida no MVP (ver
  [Riscos Técnicos](../docs/architecture/technical-risks.md)).
