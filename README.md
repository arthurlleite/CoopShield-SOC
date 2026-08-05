# CoopShield SOC

> **Projeto independente, educacional e de portfólio.** Não possui vínculo com
> instituições financeiras ou empresas reais. Todos os dados, usuários, eventos,
> transações e incidentes apresentados são sintéticos.

CoopShield SOC é uma plataforma educacional de defesa cibernética e proteção de dados
para um ambiente financeiro fictício, inspirada — de forma puramente conceitual, sem
qualquer vínculo, dado ou identidade visual real — em desafios de cooperativas
financeiras, bancos, fintechs e instituições de pagamento.

O projeto demonstra, de ponta a ponta, um fluxo de SOC (Security Operations Center):
ingestão de eventos sintéticos, proteção de dados centrada no dado (tokenização e
mascaramento), motor de detecção baseado em regras explicáveis mapeadas ao MITRE
ATT&CK, motor de risco determinístico, gestão de alertas e incidentes, playbooks
defensivos simulados e um laboratório de simulação interativo.

## Problema e Solução

Instituições financeiras processam grandes volumes de eventos de autenticação,
autorização, acesso a dados e administração. A maior parte é legítima; uma fração
pequena representa risco real. O CoopShield SOC demonstra como transformar esse volume
de eventos em sinal acionável — com segurança centrada no dado desde a ingestão — sem
nunca expor dados sensíveis (mesmo sintéticos) no processo.

Veja a [Visão do Produto](docs/product/vision.md) completa para o detalhamento do
problema, objetivos, escopo e requisitos.

## Arquitetura

O backend é um **monólito modular orientado a eventos**, construído em Java 21/Spring
Boot com arquitetura hexagonal e princípios de DDD. O frontend é uma aplicação
React/TypeScript com dois modos de operação: `DEMO_MODE` (estático, publicado no
GitHub Pages, sem backend) e `LIVE_MODE` (conectado à API real via Docker Compose
local).

Veja [docs/architecture/overview.md](docs/architecture/overview.md) para diagramas
completos (contexto, módulos, fluxo orientado a eventos, modelo de dados conceitual).

## Tecnologias

**Backend:** Java 21, Spring Boot, Spring Security, Spring Data MongoDB, Spring for
Apache Kafka, Spring Boot Actuator, Maven, OpenAPI/Swagger, JWT.

**Frontend:** React, TypeScript, Vite, React Router.

**Dados e eventos:** MongoDB, Apache Kafka, regras de detecção em YAML.

**Infraestrutura:** Docker, Docker Compose, Kubernetes educacional, Prometheus,
Grafana, OpenTelemetry.

**Testes:** JUnit 5, Mockito, AssertJ, Spring Boot Test, Testcontainers, Vitest, React
Testing Library, Playwright, k6.

**DevSecOps:** GitHub Actions, CodeQL, análise de dependências, secret scanning, SBOM.

## Estado Atual do Projeto

O projeto é desenvolvido em fases sequenciais, cada uma validada, documentada e
publicada antes do início da próxima. Estado atual:

| Fase | Status |
|------|--------|
| Fase 0 — Arquitetura e documentação inicial | Concluída |
| Fase 1 — Estrutura do back-end e front-end | Em conclusão |
| Fase 2 a 14 | Não iniciadas |

Veja o [Roadmap completo](docs/roadmap.md) para o detalhamento de todas as fases.

## Executar Localmente

Pré-requisitos: Java 21, Maven 3.9+, Node.js 20.19+/22.12+, Docker e Docker Compose.

```bash
# Backend (API em http://localhost:8080, Swagger UI em /swagger-ui.html)
cd backend
mvn spring-boot:run -pl app -am -Dspring-boot.run.profiles=local

# Frontend (em http://localhost:5173)
cd frontend
npm install
npm run dev

# Stack completa via Docker Compose (backend + frontend + MongoDB + Kafka)
cd infrastructure
cp .env.example .env
docker compose up --build
```

Veja [backend/README.md](backend/README.md) e [frontend/README.md](frontend/README.md)
para detalhes de build, testes e imagens Docker de cada parte.

## Documentação

- [Visão do Produto](docs/product/vision.md)
- [Personas e Casos de Uso](docs/product/personas-use-cases.md)
- [Modelo de Ameaça (STRIDE)](docs/threat-model/stride.md)
- [Arquitetura](docs/architecture/overview.md)
- [Perfis e Permissões](docs/architecture/roles-permissions.md)
- [Estrutura do Repositório](docs/architecture/repository-structure.md)
- [Riscos Técnicos](docs/architecture/technical-risks.md)
- [Catálogo de Eventos](docs/event-catalog/events.md)
- [Catálogo de Regras de Detecção](docs/detection-rules/catalog.md)
- [Decisões Arquiteturais (ADRs)](docs/adr/)
- [Roadmap](docs/roadmap.md)
- [Política de Segurança](SECURITY.md)
- [Guia de Contribuição](CONTRIBUTING.md)
- [Código de Conduta](CODE_OF_CONDUCT.md)

## Independência e Dados Sintéticos

Este projeto não possui vínculo com Sicoob, Itaú, Comforte, TAMUNIO, bancos,
cooperativas, empresas de segurança ou instituições financeiras reais. Não utiliza
logotipos, identidade visual proprietária, nomes de clientes ou funcionários reais,
dados bancários reais ou qualquer informação confidencial. Todos os dados, usuários,
eventos, transações e incidentes apresentados são sintéticos.

## Licença

Distribuído sob a licença [MIT](LICENSE).
