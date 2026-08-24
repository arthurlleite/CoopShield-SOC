# ADR-012: MongoDB Real na Fase 3 (Escopo: `identity` e `audit`)

- **Status:** Aceito
- **Data:** 2026-08-24

## Contexto

[ADR-011](ADR-011-persistencia-em-memoria-fase-2.md) implementou os adaptadores de
`UserRepository`, `RefreshTokenRepository` (módulo `identity`) e `AuditPort` (módulo
`audit`) em memória durante a Fase 2, para provar o fluxo de autenticação/autorização
sem exigir MongoDB configurado antes da hora. A Fase 3 (ver [Roadmap](../roadmap.md))
tem como objetivo substituir esses adaptadores por implementações reais em MongoDB.

[ADR-003](ADR-003-mongodb.md) já havia listado as 14 coleções previstas para o produto
completo (`users`, `roles`, `security_events`, `alerts`, `incidents`,
`investigation_notes`, `detection_rules`, `playbooks`, `audit_logs`, `protected_data`,
`user_baselines`, `devices`, `refresh_tokens`, `simulation_runs`). Na prática, ao chegar
na Fase 3, apenas os módulos `identity`, `accesscontrol`, `audit` e `app` têm domínio e
casos de uso implementados — os módulos `eventingestion`, `eventnormalization`,
`dataprotection`, `detection`, `risk`, `alert`, `incident`, `playbook` e `simulation`
ainda são esqueletos vazios (Fases 4 a 9). Criar coleções, documentos e índices
MongoDB para entidades cujo domínio ainda não existe violaria os critérios de conclusão
de fase (nenhum código morto, nenhuma implementação parcial — ver
[Roadmap](../roadmap.md)) e antecipia decisões de modelagem que pertencem às fases
correspondentes.

## Decisão

A Fase 3 implementa **apenas** as coleções cujos agregados já existem no código:

| Coleção | Módulo dono | Porta implementada |
|---|---|---|
| `users` | `identity` | `UserRepository` |
| `refresh_tokens` | `identity` | `RefreshTokenRepository` |
| `audit_logs` | `audit` | `AuditPort` |

Cada adaptador MongoDB (`MongoUserRepository`, `MongoRefreshTokenRepository`,
`MongoAuditLog`) segue estritamente o padrão de [ADR-009](ADR-009-arquitetura-hexagonal.md):
um documento de persistência (`UserDocument`, `RefreshTokenDocument`,
`AuditEventDocument`) que traduz de/para a entidade de domínio, sem que o domínio ou os
casos de uso (`AuthenticationService`) conheçam anotações do Spring Data. A porta e os
testes que dependem apenas da porta (unitários, com mocks) não mudaram.

As demais coleções previstas em ADR-003 (`security_events`, `alerts`, `incidents`,
`investigation_notes`, `detection_rules`, `playbooks`, `protected_data`,
`user_baselines`, `devices`, `simulation_runs`) serão criadas **na fase em que o módulo
dono do agregado for implementado**, reaplicando o mesmo padrão de adaptador
estabelecido aqui:

| Coleção futura | Módulo dono | Fase prevista |
|---|---|---|
| `simulation_runs` | `simulation` | Fase 5 |
| `detection_rules` | `detection` | Fase 6 |
| `alerts` | `alert` | Fase 8 |
| `incidents`, `investigation_notes` | `incident` | Fase 8 |
| `playbooks` | `playbook` | Fase 8 |
| `protected_data` | `dataprotection` | Fase 9 |
| `security_events` | `eventingestion`/`eventnormalization` | Fase 4 |
| `user_baselines`, `devices` | `detection`/`risk` (linha de base comportamental) | Fase 6/7 |

`roles` (também listada em ADR-003) **não** se torna uma coleção própria: perfis são um
conjunto fixo e conhecido em tempo de compilação (enum `Role` em `sharedkernel`), não um
dado mutável administrado em runtime — ver [Perfis e Permissões](../architecture/roles-permissions.md).
`Role` é armazenado como campo do documento `users`, não como referência a uma coleção
separada. Se o produto evoluir para perfis configuráveis dinamicamente, essa decisão
deve ser revisitada em um novo ADR.

### Índices e Retenção

- `users.username`: índice único (`@Indexed(unique = true)`) — usado como chave de busca
  no login e garante não haver dois usuários sintéticos com o mesmo `username`.
- `refresh_tokens.userId`: índice simples — suporta a futura revogação em massa de
  tokens de um usuário (ex.: ao bloquear a conta).
- `refresh_tokens.expiresAt`: índice **TTL** (`expireAfterSeconds = 0`) — o MongoDB
  expurga automaticamente tokens expirados ou revogados após a data de expiração; não é
  necessária uma rotina de limpeza própria da aplicação.
- `audit_logs.eventType`, `audit_logs.actor`, `audit_logs.timestamp`: índices simples —
  suportam os filtros por tipo de evento, ator e período que a interface de auditoria
  (Audit Explorer) usará ao consultar a trilha.
- `audit_logs` **não tem TTL**: a trilha de auditoria é, por definição, um registro que
  não deve expirar automaticamente enquanto o ambiente estiver ativo. Para este projeto
  educacional, a retenção é "indefinida durante a vida do ambiente de demonstração";
  uma política de arquivamento/expurgo com aprovação formal (equivalente ao que uma
  instituição real exigiria por regulação) é registrada como limitação conhecida em
  [Riscos Técnicos](../architecture/technical-risks.md), e não faz parte do escopo do
  MVP educacional.

## Alternativas Consideradas

- **Criar todas as 14 coleções já na Fase 3, com documentos "esqueleto" para as
  entidades futuras:** rejeitada. Um documento MongoDB sem caso de uso que o escreva ou
  leia é código morto disfarçado de dado, e contraria diretamente os critérios de
  conclusão de fase (proibição de placeholder e implementação parcial).
- **Adiar toda a Fase 3 até que todos os módulos de domínio existam:** rejeitada; isso
  inverteria a ordem do roadmap sem necessidade — a arquitetura hexagonal existe
  exatamente para permitir trocar o adaptador de um módulo por vez, como já provado em
  ADR-011.
- **Tornar `roles` uma coleção com um documento por perfil:** rejeitada; não há, no
  escopo do produto, necessidade de administrar perfis em runtime, e um enum é mais
  simples, mais seguro (não pode ser corrompido por um documento malformado) e
  suficiente para RBAC estático.

## Consequências

- O backend agora depende de um MongoDB real disponível (local via
  `infrastructure/docker-compose.yml`, ou efêmero via Testcontainers nos testes de
  integração); não há mais fallback em memória.
- `AbstractIntegrationTest` (módulo `app`) sobe um `MongoDBContainer` compartilhado por
  todas as subclasses da mesma JVM de teste; os testes de integração de autenticação
  precisam considerar estado persistido entre execuções e resetar explicitamente
  quando necessário (ex.: `AuthenticationFlowIntegrationTest` desbloqueia o usuário
  sintético no `finally` do teste de bloqueio de conta).
- `application-local.yml` e `infrastructure/docker-compose.yml` passam a exigir uma URI
  de MongoDB (`spring.data.mongodb.uri` / `SPRING_DATA_MONGODB_URI`); as credenciais
  usadas são sintéticas e de uso exclusivamente local (ver
  [ADR-010](ADR-010-dados-sinteticos.md) e `infrastructure/.env.example`).
- Cada fase futura que implementar um novo módulo de domínio (4 a 9) deve, como parte
  dos seus próprios critérios de conclusão, criar sua(s) coleção(ões), documento(s) de
  persistência e índices seguindo este mesmo padrão — não é necessário um novo ADR para
  cada uma, salvo se a decisão de modelagem escapar do padrão aqui estabelecido.

## Referências

- [ADR-003: MongoDB para Eventos, Alertas e Incidentes](ADR-003-mongodb.md)
- [ADR-009: Arquitetura Hexagonal](ADR-009-arquitetura-hexagonal.md)
- [ADR-011: Persistência em Memória para Identidade na Fase 2](ADR-011-persistencia-em-memoria-fase-2.md)
- [Roadmap](../roadmap.md)
- [Riscos Técnicos](../architecture/technical-risks.md)
