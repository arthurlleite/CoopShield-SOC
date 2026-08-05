# Guia de Contribuição — CoopShield SOC

> Projeto independente, educacional e de portfólio. Todos os dados, usuários, eventos,
> transações e incidentes apresentados são sintéticos.

## Antes de Contribuir

Este é primariamente um projeto de portfólio pessoal, desenvolvido em fases sequenciais
documentadas em [docs/roadmap.md](docs/roadmap.md). Contribuições externas são
bem-vindas na forma de issues (bugs, sugestões, dúvidas), mas mudanças estruturais
maiores devem ser discutidas em uma issue antes de um pull request, para alinhamento
com a fase em andamento.

## Padrões do Projeto

- **Dados exclusivamente sintéticos:** nenhuma contribuição pode introduzir dados
  reais, nomes reais, logotipos ou identidade visual de terceiros (ver
  [ADR-010](docs/adr/ADR-010-dados-sinteticos.md)).
- **Arquitetura:** respeitar os limites de módulo definidos em
  [docs/architecture/overview.md](docs/architecture/overview.md); não introduzir
  dependências circulares ou acesso direto a dados de outro módulo.
- **Segurança:** seguir as práticas descritas em [SECURITY.md](SECURITY.md); nunca
  registrar segredos, senhas, tokens ou dados sensíveis em logs.
- **Sem implementação parcial:** não são aceitas mudanças com TODOs, FIXMEs,
  placeholders, pseudocódigo ou funcionalidade documentada que não existe de fato.

## Commits

- Utilize [Conventional Commits](https://www.conventionalcommits.org/) (`feat:`,
  `fix:`, `docs:`, `test:`, `refactor:`, `chore:`, `ci:`, `build:`, `perf:`,
  `security:`).
- Commits devem ser pequenos, lógicos e relacionados a uma única mudança.
- Não inclua marcações de coautoria de ferramentas de geração de código ou IA.

## Pull Requests

1. Descreva claramente o que foi alterado e por quê.
2. Referencie a fase do roadmap relacionada, se aplicável.
3. Garanta que build e testes aplicáveis passem localmente antes de abrir o PR.
4. Atualize a documentação relacionada na mesma mudança, quando aplicável.

## Testes

A partir da Fase 1, cada camada do projeto terá suas próprias instruções de teste em
`backend/README.md`, `frontend/README.md` e `simulator/README.md`. Nenhuma
contribuição de código deve reduzir a cobertura de testes das áreas prioritárias
(domínio, segurança, regras de detecção, risco, tokenização, autorização, incidentes,
auditoria).

## Dúvidas

Abra uma issue com a etiqueta `question`.
