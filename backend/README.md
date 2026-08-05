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
(`domain` / `application` / `infrastructure`). Nesta fase (Fase 1), apenas
`sharedkernel` possui lógica de domínio real (o envelope de evento normalizado); os
demais módulos têm o esqueleto de pacotes documentado, pronto para receber a
implementação funcional das fases seguintes (ver [docs/roadmap.md](../docs/roadmap.md)).

## Pré-requisitos

- Java 21 (testado com Eclipse Temurin 21.0.12).
- Maven 3.9+ (ou utilize o wrapper, quando disponível).

## Executar Localmente

```bash
cd backend
mvn spring-boot:run -pl app -am -Dspring-boot.run.profiles=local
```

A aplicação sobe em `http://localhost:8080`, com:

- Health check: `GET /actuator/health`
- Documentação OpenAPI: `GET /v3/api-docs`
- Swagger UI: `GET /swagger-ui.html`

## Build e Testes

```bash
cd backend
mvn verify
```

Isso compila todos os 15 módulos (14 de domínio + `app`), executa os testes JUnit 5 de
cada módulo (incluindo os testes de contexto Spring Boot e dos endpoints de
health/OpenAPI) e empacota o artefato executável em `app/target/coopshield-soc.jar`.

## Perfis

| Perfil | Uso |
|--------|-----|
| `local` | Execução local fora de container, logs em nível DEBUG para o pacote do projeto |
| `docker` | Execução via Docker Compose, logs em nível INFO |

Perfis adicionais (com configuração de MongoDB e Kafka) serão introduzidos nas Fases 3
e 4, conforme o [Roadmap](../docs/roadmap.md).

## Imagem Docker

```bash
docker build -t coopshield-soc-backend -f backend/Dockerfile backend
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=docker coopshield-soc-backend
```

A imagem usa build multi-stage (Maven + JDK 21 para build, JRE 21 para runtime), roda
com usuário não privilegiado e expõe um `HEALTHCHECK` sobre `/actuator/health`.

## Limitações desta Fase

- Sem endpoints de negócio ainda (autenticação, eventos, alertas, incidentes) — chegam
  nas Fases 2 a 9.
- Sem MongoDB/Kafka configurados no backend ainda — chegam nas Fases 3 e 4.
- `mvn verify` não inclui testes de integração com Testcontainers ainda (não há
  infraestrutura de dados para testar nesta fase).
