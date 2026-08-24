# Catálogo Inicial de Regras de Detecção — CoopShield SOC

> **Aviso:** As regras abaixo avaliam exclusivamente eventos sintéticos gerados pelo
> simulador do projeto, para fins educacionais e de demonstração.

## 1. Estrutura de uma Regra (YAML)

**Implementadas na Fase 6.** As 15 regras vivem em
`backend/detection/src/main/resources/detection-rules/{categoria}/RULE-0NN.yaml` (não
em uma pasta de nível superior do repositório — ver
[ADR-014](../adr/ADR-014-motor-de-deteccao.md) sobre por que, dado que o build Docker do
backend só inclui `backend/`). O schema abaixo corresponde exatamente ao YAML real de
RULE-001, com o acréscimo de `evaluatorType` (seleciona qual avaliador do motor
interpreta `conditions` — ver ADR-014 para a lista dos sete tipos):

```yaml
id: RULE-001
name: "Múltiplas falhas de login seguidas de sucesso"
description: >
  Detecta uma sequência de falhas de autenticação seguida de um login bem-sucedido
  para o mesmo ator, dentro de uma janela curta de tempo.
enabled: true
eventTypes:
  - authentication.login.failure
  - authentication.login.success
evaluatorType: failure-then-success
conditions:
  failureEventType: authentication.login.failure
  successEventType: authentication.login.success
aggregationWindow: "PT10M"
threshold: 4
severity: HIGH
baseRiskScore: 60
mitreTactic: "Credential Access"
mitreTechnique: "T1110 - Brute Force"
recommendedPlaybook: "playbook-conta-possivelmente-comprometida"
version: "1.0.0"
falsePositiveNotes: >
  Usuários que esquecem a senha ocasionalmente podem gerar falso positivo; validar
  dispositivo e horário antes de escalar.
references:
  - "https://attack.mitre.org/techniques/T1110/"
author: "CoopShield SOC"
createdAt: "2026-08-24"
updatedAt: "2026-08-24"
```

## 2. Regras Iniciais (catálogo conceitual)

| ID | Nome | Categoria | Severidade | MITRE Technique |
|----|------|-----------|------------|-------------------|
| RULE-001 | Múltiplas falhas de login seguidas de sucesso | authentication | HIGH | T1110 - Brute Force |
| RULE-002 | Autenticação em horário atípico | authentication | MEDIUM | T1078 - Valid Accounts |
| RULE-003 | Autenticação em dispositivo desconhecido | authentication | MEDIUM | T1078 - Valid Accounts |
| RULE-004 | Viagem impossível (dados sintéticos) | authentication | HIGH | T1078 - Valid Accounts |
| RULE-005 | Acesso a endpoint fora do perfil | authorization | HIGH | T1078.004 - Cloud Accounts |
| RULE-006 | Múltiplas falhas de autorização | authorization | MEDIUM | T1069 - Permission Groups Discovery |
| RULE-007 | Consulta massiva de clientes | data-access | HIGH | T1530 - Data from Cloud Storage |
| RULE-008 | Exportação atípica de dados | data-access | HIGH | T1567 - Exfiltration Over Web Service |
| RULE-009 | Alteração administrativa seguida de acesso sensível | administration | CRITICAL | T1098 - Account Manipulation |
| RULE-010 | Envio de dado sensível em texto puro | data-access | CRITICAL | T1552 - Unsecured Credentials |
| RULE-011 | Aumento anormal de respostas HTTP 401 | api | MEDIUM | T1110 - Brute Force |
| RULE-012 | Aumento anormal de respostas HTTP 403 | api | MEDIUM | T1190 - Exploit Public-Facing Application |
| RULE-013 | Acesso por conta privilegiada fora da linha de base | administration | HIGH | T1078.003 - Local Accounts |
| RULE-014 | Sequência de ações administrativas incomuns | administration | HIGH | T1098 - Account Manipulation |
| RULE-015 | Múltiplas contas acessadas pelo mesmo dispositivo incomum | authentication | CRITICAL | T1078 - Valid Accounts |

## 3. Categorias e Organização de Arquivos

As regras estão organizadas por categoria em
`backend/detection/src/main/resources/detection-rules/`:

```
detection-rules/
├── authentication/   (RULE-001 a RULE-004, RULE-015)
├── authorization/    (RULE-005, RULE-006)
├── data-access/      (RULE-007, RULE-008, RULE-010)
├── administration/   (RULE-009, RULE-013, RULE-014)
└── api/              (RULE-011, RULE-012)
```

## 4. Explicabilidade Obrigatória

Cada correspondência (`DetectionMatch`) gerada por uma regra expõe, no mínimo:

1. qual regra foi acionada (`ruleId`, `ruleName`);
2. quais eventos contribuíram (`evidenceEventIds`);
3. qual limite foi ultrapassado (`threshold` vs. `observedValue`);
4. qual pontuação de risco (provisória, igual a `baseRiskScore` até o motor de risco da
   Fase 7 calcular o valor final explicável) e a severidade (`riskScore`, `severity`);
5. qual técnica MITRE ATT&CK foi relacionada (`mitreTactic`, `mitreTechnique`);
6. qual playbook foi recomendado (`recommendedPlaybook`).

A implementação, os testes por regra (`DetectionEngineTest`, um caso positivo e,
quando aplicável, um negativo por regra) e o motor de avaliação (sete tipos de
avaliador, ver [ADR-014](../adr/ADR-014-motor-de-deteccao.md)) foram concluídos na
Fase 6.

## Documentos Relacionados

- [Catálogo de Eventos](../event-catalog/events.md)
- [Arquitetura](../architecture/overview.md)
- [Roadmap](../roadmap.md)
