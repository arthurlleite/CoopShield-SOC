# Catálogo Inicial de Eventos — CoopShield SOC

> **Aviso:** Todos os eventos descritos neste catálogo são sintéticos, gerados pelo
> simulador do próprio projeto para fins educacionais e de demonstração.

## 1. Modelo Normalizado de Evento

Todo evento, após passar pelo módulo `eventnormalization`, segue o envelope comum:

```json
{
  "eventId": "uuid",
  "eventVersion": "1.0",
  "eventType": "authentication.login.failure",
  "timestamp": "2026-08-05T14:32:10Z",
  "source": "identity-service",
  "actor": {
    "userId": "synthetic-user-id",
    "role": "EMPLOYEE",
    "unit": "synthetic-branch-001"
  },
  "target": {
    "resourceType": "account",
    "resourceId": "synthetic-account-id"
  },
  "action": "LOGIN",
  "outcome": "FAILURE",
  "device": {
    "deviceId": "synthetic-device-id",
    "known": false
  },
  "networkContext": {
    "ipHash": "hashed-ip",
    "geo": "synthetic-region"
  },
  "dataClassification": "SENSITIVE",
  "correlationId": "uuid",
  "metadata": {}
}
```

Regras do envelope:

- `eventId` é único e imutável; o evento não é alterado após persistido — correções
  geram um novo evento com `eventVersion` incrementada e referência ao anterior.
- `correlationId` conecta eventos relacionados na mesma jornada (ex.: uma sessão).
- `dataClassification` indica se o evento contém (ou fazia referência a) dado
  classificado como sensível antes da tokenização. **Implementação atual (Fase 4):**
  até o módulo `dataprotection` existir (Fase 9), todo evento normalizado recebe
  `INTERNAL` como valor provisório e documentado — ver
  [ADR-013](../adr/ADR-013-pipeline-ingestao-normalizacao.md).
- Campos sensíveis nunca aparecem em texto puro neste envelope — são referenciados por
  token (ver [Fase 9 — Proteção de Dados](../roadmap.md)). O endereço IP de origem é um
  primeiro exemplo disso já em vigor desde a Fase 4: chega à ingestão em texto puro e é
  irreversivelmente transformado em `networkContext.ipHash` (SHA-256) pela
  normalização, antes de qualquer persistência ou publicação.

## 2. Tipos de Evento Iniciais

| `eventType` | Categoria | Descrição |
|---|---|---|
| `authentication.login.success` | Autenticação | Login bem-sucedido |
| `authentication.login.failure` | Autenticação | Falha de login |
| `authentication.logout` | Autenticação | Encerramento de sessão |
| `authentication.mfa.failure` | Autenticação | Falha de segundo fator (fictício) |
| `authorization.access.denied` | Autorização | Tentativa de acesso negada por perfil |
| `authorization.access.granted` | Autorização | Acesso concedido a recurso protegido |
| `data.access.query` | Acesso a dados | Consulta a registro de cliente/cooperado fictício |
| `data.access.export` | Acesso a dados | Exportação de dados fictícios |
| `data.access.sensitive.exposure` | Acesso a dados | Dado sensível detectado em texto puro (violação de política) |
| `api.request.received` | API | Requisição recebida em endpoint monitorado |
| `api.response.error` | API | Resposta HTTP de erro (401/403/5xx) |
| `device.recognized` | Dispositivo | Dispositivo reconhecido na linha de base do usuário |
| `device.unrecognized` | Dispositivo | Dispositivo não reconhecido |
| `admin.permission.changed` | Administração | Alteração de permissão de usuário fictício |
| `admin.account.created` | Administração | Criação de conta/usuário fictício |
| `admin.account.disabled` | Administração | Desativação de conta fictícia |
| `financial.transaction.simulated` | Financeiro fictício | Transação fictícia simulada para fins de cenário |

## 3. Tópicos Kafka

| Tópico | Propósito | Status |
|--------|-----------|--------|
| `security.raw-events` | Eventos brutos recebidos, antes de validação/normalização | Real desde a Fase 4 |
| `security.normalized-events` | Eventos normalizados, prontos para detecção | Real desde a Fase 4 |
| `security.dead-letter` | Eventos que falharam validação/processamento após as tentativas de retry | Real desde a Fase 4 |
| `security.data-policy-violations` | Violações de política de dados (ex.: dado sensível em texto puro) | Criado na Fase 9 (`dataprotection`) |
| `security.detection-alerts` | Correspondências de regra (Fase 6) e, futuramente, pontuação de risco (Fase 7) | Real desde a Fase 6 (`detection`) |
| `security.incidents` | Eventos de ciclo de vida de incidentes | Criado na Fase 8 (`incident`) |
| `security.audit-events` | Eventos de auditoria (ações privilegiadas, destokenização) | Sem fase associada ainda; `audit` persiste em MongoDB desde a Fase 3 (ver [ADR-012](../adr/ADR-012-mongodb-real-fase-3.md)) |

Ver [ADR-013](../adr/ADR-013-pipeline-ingestao-normalizacao.md) para a justificativa de
criar apenas os tópicos com produtor/consumidor real em cada fase.

## 4. Garantias de Processamento

- **Idempotência:** consumidores utilizam `eventId` para deduplicação; reprocessar o
  mesmo evento não gera efeitos colaterais duplicados (ex.: alertas duplicados).
- **Retry e Dead Letter:** falhas transitórias são reprocessadas com backoff; falhas
  persistentes são direcionadas a `security.dead-letter` com o motivo da falha.
- **Versionamento de schema:** `eventVersion` permite evolução compatível dos eventos
  sem quebrar consumidores existentes.
- **Rastreabilidade:** todo evento carrega `correlationId`, permitindo reconstruir a
  jornada completa (ingestão → normalização → detecção → alerta → incidente).

## 5. Ingestão (Fase 4)

`POST /api/v1/events` (autenticado) recebe um evento bruto, valida os campos
obrigatórios (`eventType`, `source`, `actorUserId`, `actorRole`, `targetResourceType`,
`targetResourceId`, `action`, `outcome`, `deviceId`, `sourceIp`), aplica valores padrão
aos campos opcionais ausentes (`eventId`, `eventVersion`, `timestamp`,
`correlationId`) e publica em `security.raw-events`. Retorna `202 Accepted` com
`eventId`/`correlationId`, ou `400 Bad Request` com a lista de violações. Ver
[backend/README.md](../../backend/README.md) para exemplos de requisição e
[ADR-013](../adr/ADR-013-pipeline-ingestao-normalizacao.md) para o desenho completo do
pipeline (por que ingestão e normalização não compartilham tipos Java, hashing de IP,
idempotência, dead-letter).

## Documentos Relacionados

- [Catálogo de Regras de Detecção](../detection-rules/catalog.md)
- [Arquitetura](../architecture/overview.md)
