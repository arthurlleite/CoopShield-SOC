# ADR-011: Persistência em Memória para Identidade na Fase 2

- **Status:** Aceito
- **Data:** 2026-08-05

## Contexto

O roadmap do projeto (ver [docs/roadmap.md](../roadmap.md)) posiciona a Fase 2
(Autenticação e Autorização) antes da Fase 3 (MongoDB e Modelos de Domínio). Isso
cria uma questão de ordenação: como implementar login, refresh token, bloqueio
temporário e auditoria de forma real e testável, sem ainda ter um banco de dados
configurado?

## Decisão

Os módulos `identity` e `audit` definem portas de saída (`UserRepository`,
`RefreshTokenRepository`, `AuditPort`) na camada de aplicação, e a Fase 2 implementa
essas portas com **adaptadores em memória** (`InMemoryUserRepository`,
`InMemoryRefreshTokenRepository`, `InMemoryAuditLog`), seguindo estritamente a
arquitetura hexagonal já estabelecida em [ADR-009](ADR-009-arquitetura-hexagonal.md).

A Fase 3 substituirá esses adaptadores por implementações MongoDB das mesmas portas,
sem alterar o domínio, os casos de uso (`AuthenticationService`) ou os testes que
dependem apenas das portas (os testes de unidade usam mocks; os testes de integração
usam os adaptadores em memória via o contexto Spring real).

Um usuário sintético por perfil (`SOC_ANALYST`, `SOC_MANAGER`, `EMPLOYEE`,
`BRANCH_MANAGER`, `IT_ADMIN`, `AUDITOR`) é semeado na inicialização
(`SyntheticUserSeeder`), permitindo login local/demonstrativo mesmo sem persistência
real.

## Alternativas Consideradas

- **Adiantar a Fase 3 (MongoDB) para antes da Fase 2:** rejeitada para manter a ordem
  de fases já comunicada e aprovada; além disso, a arquitetura hexagonal existe
  exatamente para permitir essa troca de adaptador sem retrabalho.
- **Usar H2/SQLite embutido como "banco temporário":** rejeitada por introduzir uma
  tecnologia de persistência (SQL) que não faz parte da stack do projeto (MongoDB),
  criando trabalho de migração desnecessário na Fase 3.

## Consequências

- Dados de usuário, refresh tokens e auditoria **não sobrevivem a um reinício da
  aplicação** durante a Fase 2. Isso é aceitável pois esta fase tem como objetivo
  provar o fluxo de autenticação/autorização, não a persistência (essa é o objetivo
  explícito da Fase 3).
- Os testes de integração que dependem de estado (ex.: bloqueio temporário) precisam
  now considerar que o contexto Spring de teste é reaproveitado entre classes de
  teste; quando um teste altera estado que outros testes não devem herdar, ele deve
  usar `@DirtiesContext` para forçar a recriação do contexto após sua execução.
- Nenhuma mudança de contrato (portas) será necessária na Fase 3 além de registrar um
  novo `@Bean` de adaptador MongoDB no lugar do adaptador em memória.

## Referências

- [ADR-009: Arquitetura Hexagonal](ADR-009-arquitetura-hexagonal.md)
- [Roadmap](../roadmap.md)
- [Perfis e Permissões](../architecture/roles-permissions.md)
