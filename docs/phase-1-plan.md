# Planejamento da Fase 1 — Estrutura do Back-end e Front-end

> Este documento é um planejamento preliminar, sujeito a ajuste no início da Fase 1.
> Nenhum item aqui descrito será implementado antes de autorização explícita para
> iniciar a Fase 1.

## Objetivo

Estabelecer o esqueleto executável do backend (Java 21/Spring Boot) e do frontend
(React/TypeScript/Vite), com a estrutura modular definida na
[Arquitetura](architecture/overview.md), pronto para receber a implementação funcional
das fases seguintes.

## Escopo Planejado

### Backend

- Projeto Maven multi-módulo (ou módulo único com pacotes por domínio — decisão a
  confirmar no início da fase e registrar como ADR se divergir do monólito modular por
  pacote) refletindo os 14 módulos definidos na arquitetura.
- Configuração inicial do Spring Boot (perfis `local`/`docker`), Actuator básico
  (`/actuator/health`).
- `Dockerfile` do backend.
- Estrutura de testes (JUnit 5, AssertJ) com um teste de contexto básico.
- Documentação OpenAPI inicial (esqueleto, sem endpoints de negócio ainda).

### Frontend

- Projeto Vite + React + TypeScript, com React Router configurado.
- Estrutura de páginas placeholder para as 12 páginas definidas na especificação do
  produto (Landing Page, SOC Dashboard, Alert Center, Incident Workspace, Detection
  Rules, Data Protection Center, Audit Explorer, Laboratory, Architecture,
  Documentation, About the Project, Login), com conteúdo mínimo real (não decorativo)
  indicando claramente que a funcionalidade completa chega em fases futuras.
- Configuração de modo claro/escuro e estrutura de componentes reutilizáveis.
- `Dockerfile` do frontend para execução local.

### Infraestrutura

- `docker-compose.yml` inicial com backend, frontend, MongoDB e Kafka, health checks e
  variáveis de ambiente documentadas.
- README com instruções de execução local.

### CI

- Workflow inicial do GitHub Actions: checkout, build do backend (Maven) e build do
  frontend (Vite), sem gates de segurança avançados ainda (esses chegam na Fase 12).

## Critérios de Conclusão Preliminares

- `mvn -f backend/pom.xml verify` (ou equivalente) executa sem falhas.
- `npm run build` no frontend executa sem falhas.
- `docker compose up` inicia todos os serviços com health checks passando.
- Nenhuma página do frontend é puramente decorativa sem indicação clara de que a
  funcionalidade real chega em fase futura.
- Documentação de backend/frontend/infraestrutura atualizada.

## Documentos Relacionados

- [Roadmap](roadmap.md)
- [Arquitetura](architecture/overview.md)
- [Estrutura do Repositório](architecture/repository-structure.md)
