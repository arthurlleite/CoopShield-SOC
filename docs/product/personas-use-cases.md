# Personas, Casos de Uso e Jornadas — CoopShield SOC

> **Aviso:** Todas as personas, nomes, unidades e cenários descritos abaixo são
> fictícios e sintéticos, criados exclusivamente para fins educacionais e de
> demonstração. Não representam pessoas ou instituições reais.

## 1. Personas

### 1.1 Ana Beatriz — Analista de SOC (`SOC_ANALYST`)
- **Objetivo:** triar alertas, investigar incidentes, aplicar playbooks, registrar
  conclusões.
- **Conhecimento:** familiarizada com MITRE ATT&CK, análise de log, triagem de alertas.
- **Frustrações que o sistema resolve:** alto volume de falsos positivos, falta de
  contexto correlacionado, dificuldade de explicar por que um alerta foi gerado.

### 1.2 Carlos Eduardo — Gerente de SOC (`SOC_MANAGER`)
- **Objetivo:** acompanhar métricas do time (MTTD/MTTA/MTTR), redistribuir carga,
  revisar incidentes críticos, aprovar encerramentos.
- **Frustrações que o sistema resolve:** falta de visão consolidada de risco e de
  desempenho do time.

### 1.3 Fernanda Lima — Atendente / Colaboradora (`EMPLOYEE`)
- **Objetivo:** realizar operações de atendimento fictícias dentro do seu perfil de
  acesso.
- **Papel no sistema:** gera parte dos eventos sintéticos normais e, em cenários de
  simulação, eventos de comportamento anômalo (ex.: consulta massiva).

### 1.4 Roberto Nogueira — Gerente de Unidade (`BRANCH_MANAGER`)
- **Objetivo:** aprovar operações de sua unidade fictícia, com acesso mais amplo que um
  atendente, porém limitado à sua carteira de clientes sintéticos.
- **Papel no sistema:** ponto de comparação de linha de base para detectar abuso de
  privilégio quando suas credenciais sintéticas são usadas fora do padrão.

### 1.5 Marina Souza — Administradora de TI (`IT_ADMIN`)
- **Objetivo:** gerenciar contas, permissões e configurações administrativas fictícias.
- **Papel no sistema:** ações administrativas (criação de conta, alteração de
  permissão) são um dos eventos de maior peso de risco quando fora do padrão.

### 1.6 Patrícia Gomes — Auditora (`AUDITOR`)
- **Objetivo:** consultar trilhas de auditoria, validar conformidade de acesso a dados
  sensíveis, revisar tentativas de destokenização.
- **Papel no sistema:** consumidora primária do módulo de auditoria; não realiza
  triagem operacional de alertas.

## 2. Casos de Uso

| ID | Caso de uso | Persona primária |
|----|-------------|-------------------|
| UC-01 | Autenticar-se na plataforma com JWT de curta duração | Todos os perfis |
| UC-02 | Ingerir evento sintético via API/Kafka | Sistema (produtores) |
| UC-03 | Normalizar e classificar evento recebido | Sistema (event normalization) |
| UC-04 | Identificar e tokenizar dado sensível antes da persistência | Sistema (data protection) |
| UC-05 | Solicitar destokenização com justificativa | SOC_ANALYST, AUDITOR |
| UC-06 | Avaliar evento contra regras de detecção YAML | Sistema (detection engine) |
| UC-07 | Calcular pontuação de risco explicável para um alerta | Sistema (risk engine) |
| UC-08 | Visualizar e filtrar alertas ativos | SOC_ANALYST, SOC_MANAGER |
| UC-09 | Atribuir e alterar status de um alerta | SOC_ANALYST |
| UC-10 | Promover alerta(s) para incidente | SOC_ANALYST, SOC_MANAGER |
| UC-11 | Investigar incidente: evidências, linha do tempo, notas | SOC_ANALYST |
| UC-12 | Selecionar e executar playbook defensivo simulado | SOC_ANALYST |
| UC-13 | Encerrar incidente com conclusão e classificação final | SOC_ANALYST, SOC_MANAGER |
| UC-14 | Consultar trilha de auditoria | AUDITOR, SOC_MANAGER |
| UC-15 | Consultar dashboard de métricas de SOC | SOC_MANAGER, SOC_ANALYST |
| UC-16 | Executar cenário no laboratório de simulação | Todos (fins de demonstração) |
| UC-17 | Executar laboratório em modo demonstração (GitHub Pages, sem backend) | Visitante/recrutador |
| UC-18 | Gerenciar regras de detecção versionadas (visualização) | SOC_MANAGER, AUDITOR |

## 3. Jornadas Principais

### Jornada A — Do evento bruto ao incidente encerrado
1. O simulador gera um cenário sintético de "conta possivelmente comprometida" (várias
   falhas de autenticação seguidas de sucesso, em dispositivo desconhecido).
2. Os eventos são publicados em `security.raw-events`.
3. O módulo de normalização valida, classifica e publica em
   `security.normalized-events`.
4. O módulo de proteção de dados identifica campos sensíveis (ex.: identificador de
   cliente fictício) e os tokeniza antes de qualquer persistência ou publicação de
   análise.
5. O motor de detecção avalia a regra "várias falhas de login seguidas de sucesso" e a
   regra "autenticação em dispositivo desconhecido"; ambas são acionadas.
6. O motor de risco calcula a pontuação combinando severidade da regra, dispositivo
   desconhecido e reincidência, chegando a um score explicável (ex.: 85/100).
7. Um alerta é criado com evidências, fatores de risco, mapeamento MITRE ATT&CK e
   playbook recomendado.
8. Ana Beatriz (SOC_ANALYST) visualiza o alerta no Alert Center, reconhece, investiga e
   promove para incidente.
9. No Incident Workspace, ela adiciona notas, revisa a linha do tempo, aplica o
   playbook "conta possivelmente comprometida" (ações simuladas: revogar sessão,
   solicitar redefinição de credenciais) e encerra o incidente com uma conclusão.
10. Toda a jornada fica registrada em auditoria e refletida nas métricas do dashboard
    (MTTD/MTTA/MTTR simulados).

### Jornada B — Consulta de auditoria e destokenização controlada
1. Patrícia Gomes (AUDITOR) identifica, na trilha de auditoria, uma tentativa de
   destokenização registrada por Ana Beatriz durante a investigação da Jornada A.
2. Ela consulta a justificativa registrada, o perfil que autorizou e o horário da
   operação.
3. Ela confirma que a destokenização seguiu a política (autorização + justificativa +
   perfil permitido) e não identifica não conformidade.

### Jornada C — Visitante explorando o modo demonstração
1. Um recrutador acessa o GitHub Pages do projeto.
2. A landing page apresenta o problema, a solução, a arquitetura e o aviso de
   independência/dados sintéticos.
3. O visitante entra no modo demonstração (DEMO_MODE), que carrega eventos e cenários
   de arquivos JSON estáticos, sem exigir backend nem autenticação real.
4. Ele executa um cenário no laboratório e observa, no navegador, o fluxo completo:
   evento bruto → normalizado → tokenizado → regra acionada → risco → alerta →
   incidente → linha do tempo.

## Documentos Relacionados

- [Visão do Produto](vision.md)
- [Modelo de Ameaça STRIDE](../threat-model/stride.md)
- [Perfis e Permissões](../architecture/roles-permissions.md)
