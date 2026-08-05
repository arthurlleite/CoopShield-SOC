# ADR-003: MongoDB para Eventos, Alertas e Incidentes

- **Status:** Aceito
- **Data:** 2026-08-05

## Contexto

Os principais agregados do sistema (eventos de segurança, alertas, incidentes, notas de
investigação, regras de detecção, playbooks, auditoria, dados protegidos, baselines de
usuário, dispositivos, refresh tokens, execuções de simulação) têm estruturas
semi-estruturadas, que variam por tipo de evento e evoluem ao longo do tempo
(`eventVersion`), e não exigem transações multi-tabela complexas no MVP.

## Decisão

Utilizar **MongoDB** como banco de dados principal, com as coleções listadas na seção
19 da especificação do produto (`users`, `roles`, `security_events`, `alerts`,
`incidents`, `investigation_notes`, `detection_rules`, `playbooks`, `audit_logs`,
`protected_data`, `user_baselines`, `devices`, `refresh_tokens`, `simulation_runs`),
com índices em `timestamp`, `actorId`, `correlationId`, `status`, `severity`, `ruleId`,
`incidentId`, `alertId`, `eventType`, `deviceId` e `assignedAnalyst`.

## Alternativas Consideradas

- **Banco relacional (PostgreSQL):** viável, mas exigiria um esquema mais rígido para
  eventos cuja estrutura varia por `eventType` e evolui por versão; MongoDB se encaixa
  melhor no modelo de documento imutável descrito no catálogo de eventos.
- **Banco de séries temporais dedicado:** rejeitado no MVP por adicionar mais uma peça
  de infraestrutura sem necessidade comprovada neste estágio; pode ser revisitado no
  roadmap se o volume de eventos justificar.

## Consequências

- Necessário documentar estratégia de retenção/expiração (TTL) para eventos e logs de
  auditoria, tratada em detalhe na Fase 3.
- Necessário Testcontainers com MongoDB para testes de integração.
- Immutabilidade de eventos após persistência é uma regra de aplicação, não uma garantia
  nativa do banco — deve ser reforçada no código do módulo `eventnormalization`.

## Referências

- [Arquitetura](../architecture/overview.md)
- [Catálogo de Eventos](../event-catalog/events.md)
