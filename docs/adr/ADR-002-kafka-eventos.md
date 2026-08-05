# ADR-002: Apache Kafka como Backbone de Eventos

- **Status:** Aceito
- **Data:** 2026-08-05

## Contexto

O sistema precisa processar um fluxo contínuo de eventos sintéticos (autenticação,
autorização, acesso a dados, API, dispositivos, administração, financeiro fictício) de
forma desacoplada entre produtores (simulador, ingestão) e consumidores (normalização,
detecção, auditoria), com garantias de reprocessamento, ordenação por chave e
tolerância a falhas.

## Decisão

Utilizar **Apache Kafka** como backbone de eventos entre os módulos do backend, com os
tópicos definidos no [Catálogo de Eventos](../event-catalog/events.md):
`security.raw-events`, `security.normalized-events`, `security.data-policy-violations`,
`security.detection-alerts`, `security.incidents`, `security.audit-events` e
`security.dead-letter`.

## Alternativas Consideradas

- **Apenas fila simples (ex.: RabbitMQ) ou chamadas síncronas entre módulos:**
  rejeitada por não demonstrar tão bem os padrões de arquitetura orientada a eventos
  (replay, partições, consumer groups) relevantes para o perfil de vaga que o projeto
  visa demonstrar (SOC/engenharia de detecção lida tipicamente com Kafka/streaming).
- **Apenas eventos internos in-process (sem broker):** rejeitada por não permitir
  demonstrar produção/consumo real, idempotência e dead-letter em um cenário
  distribuído, mesmo que simulado localmente.

## Consequências

- Necessário implementar idempotência (dedução por `eventId`), retry com backoff e
  dead-letter topic.
- Necessário Testcontainers para testes de integração de produtores/consumidores.
- Introduz Kafka como dependência de infraestrutura obrigatória no modo completo
  (Docker Compose); o modo demonstração (GitHub Pages) não depende de Kafka, usando
  arquivos JSON estáticos (ver [ADR-007](ADR-007-demo-live-mode.md)).

## Referências

- [Catálogo de Eventos](../event-catalog/events.md)
- [Arquitetura](../architecture/overview.md)
