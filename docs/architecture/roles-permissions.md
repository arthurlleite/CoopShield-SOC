# Perfis e Permissões — CoopShield SOC

> **Aviso:** Todos os perfis e permissões descritos referem-se a um sistema
> educacional que processa exclusivamente dados sintéticos.

## 1. Perfis (Roles)

| Perfil | Descrição |
|--------|-----------|
| `SOC_ANALYST` | Analista de SOC: triagem, investigação, aplicação de playbooks |
| `SOC_MANAGER` | Gerente de SOC: visão consolidada, métricas, aprovação de encerramentos |
| `EMPLOYEE` | Colaborador fictício: gera eventos operacionais normais/anômalos no simulador |
| `BRANCH_MANAGER` | Gerente de unidade fictícia: acesso mais amplo que `EMPLOYEE`, limitado à sua carteira sintética |
| `IT_ADMIN` | Administrador de TI fictício: ações administrativas (contas, permissões) |
| `AUDITOR` | Auditor: leitura de trilhas de auditoria e revisão de destokenização |

Princípio aplicado: **menor privilégio**. Cada perfil recebe apenas as permissões
estritamente necessárias às suas responsabilidades.

## 2. Matriz de Permissões (visão conceitual da Fase 0)

A matriz detalhada por endpoint será formalizada na Fase 2 (Autenticação e
Autorização), junto ao código de autorização. A visão conceitual desta fase é:

| Capacidade | SOC_ANALYST | SOC_MANAGER | EMPLOYEE | BRANCH_MANAGER | IT_ADMIN | AUDITOR |
|---|---|---|---|---|---|---|
| Visualizar dashboard SOC | ✔ | ✔ | ✘ | ✘ | ✘ | ✔ (somente leitura) |
| Visualizar/gerenciar alertas | ✔ | ✔ | ✘ | ✘ | ✘ | ✘ |
| Investigar/gerenciar incidentes | ✔ | ✔ | ✘ | ✘ | ✘ | ✘ |
| Encerrar incidente | ✔ | ✔ | ✘ | ✘ | ✘ | ✘ |
| Visualizar regras de detecção | ✔ | ✔ | ✘ | ✘ | ✘ | ✔ |
| Solicitar destokenização | ✔ (com justificativa) | ✔ (com justificativa) | ✘ | ✘ | ✘ | ✔ (com justificativa) |
| Consultar trilha de auditoria | ✘ | ✔ | ✘ | ✘ | ✘ | ✔ |
| Executar laboratório de simulação | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| Ações administrativas fictícias (contas/permissões) | ✘ | ✘ | ✘ | ✘ | ✔ | ✘ |
| Operações fictícias de atendimento/unidade | ✘ | ✘ | ✔ | ✔ | ✘ | ✘ |

## 3. Regras Gerais de Autorização

- Todo endpoint autenticado exige um perfil explicitamente autorizado; ausência de
  anotação de autorização é tratada como erro de configuração, não como acesso liberado.
- Toda ação privilegiada (administrativa, destokenização, encerramento de incidente) é
  registrada em auditoria com `actor`, `action`, `justification` (quando aplicável) e
  `correlationId`.
- Falhas de autorização são registradas (sem dado sensível) e contam para a regra de
  detecção "várias falhas de autorização".
- Mensagens de erro de autenticação/autorização são genéricas, para impedir enumeração
  de usuários ou de recursos existentes.

## Documentos Relacionados

- [Personas e Casos de Uso](../product/personas-use-cases.md)
- [Modelo de Ameaça STRIDE](../threat-model/stride.md)
- [Arquitetura](overview.md)
