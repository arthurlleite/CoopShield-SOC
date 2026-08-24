# ADR-013: Pipeline de Ingestão e Normalização de Eventos (Fase 4)

- **Status:** Aceito
- **Data:** 2026-08-24

## Contexto

A Fase 4 implementa os módulos `eventingestion` e `eventnormalization` (ver
[Roadmap](../roadmap.md)) e os torna reais pela primeira vez: recepção de eventos
sintéticos brutos, publicação/consumo via Kafka, normalização para o envelope comum
(`EventEnvelope`, definido em [ADR-002](ADR-002-kafka-eventos.md) e
[Catálogo de Eventos](../event-catalog/events.md)), idempotência e dead-letter.

Duas restrições de domínio já existentes precisavam ser respeitadas:

1. `NetworkContext` (sharedkernel) proíbe, em seu construtor, um IP em texto puro —
   exige `ipHash`.
2. `EventEnvelope.dataClassification` é, por design (ver javadoc do tipo), atribuída
   pelo módulo `dataprotection` — que só chega na Fase 9.

Isso significa que um evento **não pode** nascer já como `EventEnvelope`: alguém
precisa transformá-lo (hash do IP) e alguém precisa decidir uma classificação
provisória até `dataprotection` existir.

## Decisão

### Separação real entre bruto e normalizado

`eventingestion` **não** produz `EventEnvelope`. Define seu próprio tipo de domínio,
`RawEvent`, que carrega `sourceIp` em texto puro (a última vez que isso acontece no
pipeline) e não carrega `dataClassification`. `POST /api/v1/events` valida os campos
obrigatórios, aplica valores padrão (`eventId`, `eventVersion`, `timestamp`,
`correlationId` quando ausentes) e publica em `security.raw-events`.

`eventnormalization` consome `security.raw-events`, e é o **único** lugar do sistema
que:

- aplica SHA-256 ao IP de origem (`IpHasher`, mesmo padrão de
  `RefreshTokenSecretHasher` do módulo `identity`) antes de construir `NetworkContext`;
- atribui `DataClassification.INTERNAL` como valor **provisório e documentado** a todo
  evento, até `dataprotection` (Fase 9) assumir essa responsabilidade de verdade;
- persiste de forma idempotente (ver abaixo) e publica em
  `security.normalized-events`.

### Nenhum tipo Java compartilhado entre os dois módulos

`eventingestion` e `eventnormalization` **não têm dependência Maven um do outro**. O
contrato entre eles é o JSON publicado no tópico `security.raw-events`, não um tipo
Java. Cada módulo define sua própria forma de serialização/deserialização
(`RawEventMessage`, duplicado propositalmente nos dois módulos). Isso é a demonstração
mais direta possível dos limites de módulo orientados a evento descritos em
[docs/architecture/overview.md](../architecture/overview.md): um consumidor nunca deve
depender do tipo de domínio interno de um produtor.

Pelo mesmo motivo, nenhum dos dois serializa os records de `sharedkernel`
(`EventEnvelope`, `EventId`, `CorrelationId`) diretamente: `sharedkernel` declara
explicitamente, em seu `pom.xml`, que não depende de nenhum framework externo — logo
não pode carregar anotações Jackson. `EventId`/`CorrelationId` sem anotação
serializariam como objeto aninhado (`{"value":"uuid"}`), não como string simples. Cada
módulo usa um DTO de serialização próprio (`RawEventMessage`, `NormalizedEventMessage`)
que traduz explicitamente para o formato de string plano do catálogo de eventos.

### Idempotência

A coleção `security_events` (reservada para a Fase 4 em
[ADR-012](ADR-012-mongodb-real-fase-3.md)) usa `eventId` como `_id` (chave natural do
MongoDB, unicidade garantida nativamente). `MongoNormalizedEventRepository` usa
`insert` — nunca `save`/upsert — então uma tentativa de reprocessar o mesmo `eventId`
falha com `DuplicateKeyException`, tratada como "já processado", não como erro. O
evento normalizado só é publicado em `security.normalized-events` quando a inserção é
a primeira (evita reemitir o mesmo evento normalizado a cada reprocessamento).

Consequência: o consumidor Kafka pode usar `enable.auto.commit=true` e
`auto-offset-reset=earliest` sem risco de duplicar efeitos colaterais — a garantia de
"exactly-once" do ponto de vista do domínio vem do índice único do Mongo, não da
semântica de offset do Kafka (que continua sendo "at-least-once").

`auto.offset.reset=earliest` é fixado diretamente no `@KafkaListener`
(`properties = "auto.offset.reset=earliest"`), não apenas em `application.yml` do
módulo `app`. Depender só da configuração externa causou uma falha real detectada
durante o desenvolvimento desta fase: o padrão do cliente Kafka é `latest`, e nos
testes de integração do módulo `eventnormalization` isolado (sem o `application.yml`
do `app` no classpath) o consumidor por vezes completava seu primeiro reset de offset
**depois** que a mensagem de teste já havia sido publicada — "latest" nesse instante
apontava para depois da mensagem, que era então permanentemente ignorada. Fixar a
propriedade no próprio listener torna a garantia de idempotência uma característica do
módulo, não um acidente de configuração de quem o compõe.

### Retry e Dead Letter

`EventNormalizationException` (campo obrigatório ausente, JSON malformado, enum
inválido) é tratada como falha **permanente**: reprocessar a mesma mensagem produz o
mesmo erro, então o `DefaultErrorHandler` do listener a envia direto para
`security.dead-letter`, sem retry (`addNotRetryableExceptions`). Qualquer outra exceção
(ex.: MongoDB temporariamente indisponível) é tratada como transiente: duas tentativas
com 500ms de intervalo (`FixedBackOff`) antes de também cair no dead-letter.

### Tópicos criados nesta fase

Apenas os tópicos com um produtor/consumidor real nesta fase são declarados
(`NewTopic` bean) e testados:

| Tópico | Produzido por | Consumido por |
|---|---|---|
| `security.raw-events` | `eventingestion` | `eventnormalization` |
| `security.normalized-events` | `eventnormalization` | (Fase 6 — `detection`) |
| `security.dead-letter` | `eventnormalization` (error handler) | (revisão manual/futura ferramenta) |

Os demais tópicos do catálogo original (`security.data-policy-violations`,
`security.detection-alerts`, `security.incidents`, `security.audit-events`) continuam
sem produtor/consumidor real e serão criados nas fases em que seus módulos donos forem
implementados — mesma disciplina de ADR-012 para coleções MongoDB, aplicada agora a
tópicos Kafka.

## Alternativas Consideradas

- **Ingestão já publicar um `EventEnvelope`:** rejeitada; violaria diretamente o
  invariante de `NetworkContext` (proibe IP em texto puro) sem antes fazer o hash, e
  obrigaria a ingestão a decidir uma classificação de dados — responsabilidade do
  módulo `dataprotection`, que ainda não existe.
- **Compartilhar `RawEventMessage` como dependência Maven entre os dois módulos:**
  rejeitada; acoplaria um consumidor ao tipo interno de um produtor, contrariando o
  desenho de módulos orientados a evento já documentado. O custo (duplicar um pequeno
  DTO) é menor que o acoplamento que essa alternativa introduziria.
- **Usar `save` (upsert) em vez de `insert` para a idempotência:** rejeitada; um upsert
  bem-sucedido não distingue "primeira vez" de "reprocessamento", exigindo uma
  consulta extra (`existsById` antes do `save`) que não é atômica e permitiria uma
  corrida entre duas instâncias do consumidor. `insert` com captura de
  `DuplicateKeyException` é atômico no nível do banco.

## Consequências

- O backend agora depende de um Kafka real (local via
  `infrastructure/docker-compose.yml`, ou efêmero via Testcontainers nos testes).
- Os testes de integração com Kafka usam a imagem `confluentinc/cp-kafka` (não
  `apache/kafka`, usada em produção/docker-compose): a classe nativa
  `org.testcontainers.kafka.KafkaContainer` com imagens `apache/kafka` falhou em
  detectar o `advertised.listener` correto neste ambiente (Docker Desktop/Windows,
  erro `advertised.listeners cannot use the nonroutable meta-address 0.0.0.0`);
  `org.testcontainers.containers.KafkaContainer` (Confluent) é a combinação mais
  testada do ecossistema Testcontainers e não apresenta o problema. O protocolo Kafka
  exercitado é idêntico ao usado em produção.
- Quando `dataprotection` (Fase 9) for implementado, `EventNormalizationService`
  precisará delegar a classificação a uma porta nova (`DataClassifier` ou similar) em
  vez do valor fixo `DataClassification.INTERNAL` — mudança pontual e já isolada em um
  único ponto do código.

## Referências

- [ADR-002: Kafka para Eventos](ADR-002-kafka-eventos.md)
- [ADR-009: Arquitetura Hexagonal](ADR-009-arquitetura-hexagonal.md)
- [ADR-012: MongoDB Real na Fase 3](ADR-012-mongodb-real-fase-3.md)
- [Catálogo de Eventos](../event-catalog/events.md)
- [Arquitetura](../architecture/overview.md)
