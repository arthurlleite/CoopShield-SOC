# ADR-014: Motor de Detecção — Avaliadores, Histórico e Correspondências

- **Status:** Aceito
- **Data:** 2026-08-24

## Contexto

A Fase 6 implementa o módulo `detection`: carrega as 15 regras iniciais de YAML (ver
[ADR-006](ADR-006-regras-yaml.md) e [Catálogo de Regras](../detection-rules/catalog.md)),
consome `security.normalized-events` e avalia cada evento contra as regras habilitadas.
Isso exige responder a três perguntas de design que o catálogo original deixou em
aberto: (1) como interpretar `conditions`, um mapa genérico, de forma tipada; (2) onde
vive o estado necessário para regras que dependem de agregação (contagem, sequência);
(3) o que fazer com o resultado, já que "alerta" completo só existe na Fase 8.

## Decisão

### Sete tipos de avaliador, selecionados por `evaluatorType`

Cada regra YAML ganhou um campo `evaluatorType` (acréscimo ao schema do catálogo
original) que seleciona qual `RuleEvaluator` interpreta `conditions`:

| `evaluatorType` | Regras | O que verifica |
|---|---|---|
| `failure-then-success` | RULE-001 | N falhas seguidas de um sucesso, mesmo ator |
| `atypical-hour` | RULE-002 | `metadata.hourOfDay` fora do horário comercial |
| `single-event-flag` | RULE-003, 005, 010, 013 | O próprio evento (ou um campo dele) já é o sinal — sem agregação |
| `impossible-travel` | RULE-004 | Região sintética diferente entre logins consecutivos do mesmo ator |
| `threshold-count` | RULE-006, 007, 008, 011, 012, 014 | N eventos (opcionalmente filtrados por metadata) do mesmo ator na janela |
| `sequence-two-types` | RULE-009 | Um evento de um segundo tipo precedido por um evento de um primeiro tipo, mesmo ator |
| `same-device-multiple-accounts` | RULE-015 | N atores distintos usando o mesmo dispositivo na janela |

Os avaliadores são **puros**: recebem a regra, o evento atual e o histórico relevante já
filtrado (nunca consultam banco/Kafka diretamente), o que os torna triviais de testar
unitariamente — ver `DetectionEngineTest`, que cobre as 15 regras com um caso positivo e,
quando fizer sentido, um caso negativo (evitando falso positivo).

### Histórico em memória, por instância

`EventHistory` (porta) mantém uma janela deslizante por ator e por dispositivo (`conditions.aggregationKey: device`
seleciona a segunda). A implementação (`InMemoryEventHistory`) é **em memória, de uma
única instância**: perde o estado a um reinício do processo e não escala
horizontalmente. Aceitável para o MVP educacional (uma única instância de backend, ver
[ADR-001](ADR-001-monolito-modular.md)); um deploy com múltiplas réplicas exigiria um
armazenamento distribuído (ex.: Kafka Streams state store, Redis) — registrado como
limitação conhecida, não como pendência desta fase.

O motor consulta o histórico **antes** de registrar o evento atual nele, para que cada
regra veja apenas o que aconteceu antes do evento sendo avaliado.

### Correspondência explicável como saída da fase, não "alerta"

`DetectionMatch` carrega tudo exigido pela seção "Explicabilidade Obrigatória" do
catálogo (regra, evidências, limite vs. valor observado, mapeamento MITRE, playbook
recomendado), mas **não é** o `Alert` da Fase 8 — esse ciclo de vida (NEW → ACKNOWLEDGED
→ ...) pertence exclusivamente ao módulo `alert`. `riskScore` usa `baseRiskScore` da
regra como valor provisório, documentado explicitamente: o cálculo explicável
combinando severidade, dispositivo, volume e reincidência é responsabilidade do motor de
risco (Fase 7, ainda não implementado) — mesmo padrão de classificação provisória já
usado em `EventNormalizationService` (ver [ADR-013](ADR-013-pipeline-ingestao-normalizacao.md)).

Cada correspondência é persistida em `detection_matches` (reservada, junto de
`detection_rules`, para esta fase em [ADR-012](ADR-012-mongodb-real-fase-3.md)) e
publicada em `security.detection-alerts` — o motor de risco (Fase 7) consumirá esse
tópico para calcular a pontuação final, e o módulo de alerta (Fase 8) o consumirá (ou
consumirá a saída do risco) para criar o alerta persistente.

### YAML dentro do módulo, não em uma pasta de nível superior

As 15 regras vivem em `backend/detection/src/main/resources/detection-rules/{categoria}/RULE-0NN.yaml`,
não em uma pasta `detection-rules/` na raiz do repositório como o texto original de
`docs/architecture/repository-structure.md` sugeria. Motivo: o contexto de build da
imagem Docker do backend é apenas `backend/` (ver `infrastructure/docker-compose.yml`);
qualquer YAML fora desse diretório não estaria disponível dentro do container em
produção/demonstração. As regras continuam "versionadas no repositório" (ADR-006) —
apenas em um caminho que garante que o carregador as encontre em qualquer ambiente de
execução.

### Chave de partição Kafka corrigida: `correlationId`, não `eventId`

Durante o desenvolvimento desta fase, o teste de ponta a ponta (cenário
"conta possivelmente comprometida" → RULE-001) revelou um bug real introduzido nas
Fases 4/5: `KafkaRawEventPublisher` e `KafkaNormalizedEventPublisher` publicavam usando
`eventId` (aleatório por evento) como chave de partição. Como os tópicos têm múltiplas
partições, o Kafka só garante ordem **dentro** de uma partição — eventos da mesma
jornada podiam cair em partições diferentes e chegar ao motor de detecção fora de
ordem, quebrando qualquer regra que dependa de sequência (RULE-001 seria o caso mais
visível, mas todo `evaluatorType` que lê histórico depende de ordem). Corrigido para
particionar por `correlationId` em ambos os publishers — todos os eventos de uma mesma
jornada agora caem sempre na mesma partição, na ordem em que foram publicados.

## Alternativas Consideradas

- **Motor de regras genérico (Drools, Easy Rules):** rejeitado desde ADR-006 por
  desproporção de complexidade frente a 15 regras conhecidas.
- **Persistir o histórico de agregação no MongoDB (`security_events`, de
  `eventnormalization`):** rejeitada; exigiria que `detection` lesse a coleção de outro
  módulo diretamente, violando o limite de domínio documentado em
  [docs/architecture/overview.md](../architecture/overview.md) ("nenhum módulo acessa a
  coleção MongoDB de outro módulo diretamente"). Um histórico próprio, em memória, mantém
  `detection` desacoplado de `eventnormalization`.
- **`DetectionMatch` já ser o `Alert` final:** rejeitada; anteciparia o desenho do ciclo
  de vida de alerta (Fase 8) e do cálculo de risco (Fase 7) para esta fase, violando a
  separação de responsabilidades já definida na arquitetura (seção "Módulos do Backend").

## Consequências

- `detection` não depende de nenhum outro módulo além de `sharedkernel` (nem
  `eventingestion`, nem `eventnormalization`) — o contrato é inteiramente via Kafka
  (`security.normalized-events` de entrada, `security.detection-alerts` de saída),
  reforçando o padrão de módulos orientados a evento já estabelecido.
- A correção da chave de partição em `eventingestion`/`eventnormalization` é
  retroativamente aplicável às Fases 4 e 5: o comportamento observável (JSON publicado)
  não muda, apenas a chave de partição Kafka.
- Testes de integração que buscavam mensagens pela chave (`record.key()`) precisaram ser
  ajustados para buscar pelo conteúdo, já que a chave deixou de ser o `eventId`.

## Referências

- [ADR-006: Regras de Detecção Explicáveis em YAML](ADR-006-regras-yaml.md)
- [ADR-012: MongoDB Real na Fase 3](ADR-012-mongodb-real-fase-3.md)
- [ADR-013: Pipeline de Ingestão e Normalização](ADR-013-pipeline-ingestao-normalizacao.md)
- [Catálogo de Regras de Detecção](../detection-rules/catalog.md)
- [Arquitetura](../architecture/overview.md)
