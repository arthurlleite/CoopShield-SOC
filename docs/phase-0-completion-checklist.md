# Checklist de Conclusão — Fase 0

## Documentos Entregues

- [x] Visão do produto, problema, objetivos, escopo e fora de escopo — [docs/product/vision.md](product/vision.md)
- [x] Requisitos funcionais e não funcionais — [docs/product/vision.md](product/vision.md)
- [x] Personas, casos de uso e jornadas principais — [docs/product/personas-use-cases.md](product/personas-use-cases.md)
- [x] Modelo de ameaça STRIDE, ativos protegidos, agentes de ameaça, superfícies de ataque, controles propostos — [docs/threat-model/stride.md](threat-model/stride.md)
- [x] Arquitetura, módulos, limites de domínio, modelo de dados conceitual, diagramas Mermaid — [docs/architecture/overview.md](architecture/overview.md)
- [x] Perfis e permissões — [docs/architecture/roles-permissions.md](architecture/roles-permissions.md)
- [x] Catálogo inicial de eventos — [docs/event-catalog/events.md](event-catalog/events.md)
- [x] Catálogo inicial de regras de detecção — [docs/detection-rules/catalog.md](detection-rules/catalog.md)
- [x] Estrutura do repositório — [docs/architecture/repository-structure.md](architecture/repository-structure.md)
- [x] Riscos técnicos — [docs/architecture/technical-risks.md](architecture/technical-risks.md)
- [x] ADRs 001 a 010 — [docs/adr/](adr/)
- [x] Roadmap com as 15 fases e critérios de conclusão — [docs/roadmap.md](roadmap.md)
- [x] Planejamento da Fase 1 — [docs/phase-1-plan.md](phase-1-plan.md)
- [x] README.md principal atualizado com aviso de independência e visão geral
- [x] SECURITY.md
- [x] CONTRIBUTING.md
- [x] CODE_OF_CONDUCT.md
- [x] LICENSE

## Verificações de Qualidade

- [x] Nenhum placeholder, TODO, FIXME ou "implementar depois" nos documentos entregues.
- [x] Nenhum arquivo vazio.
- [x] Aviso de independência e dados sintéticos presente em todos os documentos que
      descrevem cenários, personas ou dados de exemplo.
- [x] Nenhum dado real, nome real, logotipo ou identidade visual de terceiros utilizado.
- [x] Todos os diagramas Mermaid validados manualmente quanto à sintaxe (flowchart,
      sequenceDiagram, erDiagram).
- [x] Todos os links internos entre documentos apontam para arquivos existentes no
      próprio commit.
- [x] Documentos coerentes entre si (mesma nomenclatura de módulos, perfis, eventos e
      regras em todos os documentos).
- [ ] Não aplicável nesta fase: build, testes automatizados, containers, pipeline de CI
      — não há código funcional na Fase 0, apenas documentação. Essas verificações
      passam a existir a partir da Fase 1, conforme o [Roadmap](roadmap.md).

## Git

- [x] `git config user.name` e `git config user.email` confirmados antes do primeiro
      commit.
- [x] Commits organizados por Conventional Commits, sem coautoria de ferramenta/IA.
- [x] Nenhum segredo, credencial ou dado sensível nos arquivos commitados.
- [x] Working tree limpo antes da publicação.
- [x] Push realizado para o branch remoto correspondente, sem force push.

## Conclusão

Todos os itens obrigatórios da Fase 0 (documentação de arquitetura e planejamento
inicial) foram entregues e revisados. Itens de build/teste/pipeline são marcados como
"não aplicável" nesta fase por não fazerem parte do escopo da Fase 0 — eles são
critérios de conclusão da Fase 1 em diante, conforme o [Roadmap](roadmap.md).
