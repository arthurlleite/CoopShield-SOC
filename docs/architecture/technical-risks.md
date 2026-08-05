# Riscos Técnicos — CoopShield SOC

> Este documento registra riscos técnicos identificados na Fase 0. Riscos que se
> concretizarem ou novos riscos identificados em fases futuras devem ser adicionados
> aqui, mantendo o histórico de mitigação.

| ID | Risco | Impacto | Probabilidade | Mitigação Planejada |
|----|-------|---------|----------------|------------------------|
| R-01 | Complexidade do monólito modular crescer a ponto de violar limites de domínio (acoplamento acidental entre módulos) | Alto | Média | Revisão arquitetural por fase; testes de arquitetura (ex.: ArchUnit) a partir da Fase 1 |
| R-02 | Volume de escopo (14 fases, múltiplas tecnologias) levar a implementações parciais sob pressão de tempo | Alto | Média | Critérios de conclusão rígidos por fase (seção 3 das regras de execução); nenhuma fase avança sem 100% dos critérios atendidos |
| R-03 | Testcontainers (Kafka/MongoDB) tornarem o CI lento ou instável | Médio | Média | Uso de imagens leves, paralelização de jobs, cache de dependências no CI (Fase 12) |
| R-04 | Divergência entre Demo Mode e Live Mode (funcionalidade demonstrada publicamente não refletir o sistema real) | Alto | Média | Interface de acesso a dados comum entre os dois modos (ADR-007); testes cobrindo ambos os modos |
| R-05 | Exposição acidental de dado sensível (mesmo sintético) em log, erro ou evidência de alerta | Alto | Baixa | Testes automatizados de ausência de dado sensível em logs (Fase 9/12); revisão de código obrigatória |
| R-06 | Chaves/segredos de tokenização geridos localmente (variável de ambiente) não serem adequados além do MVP educacional | Médio | Alta (esperado) | Documentado explicitamente como limitação do MVP, com caminho de evolução para Vault/KMS no roadmap |
| R-07 | Regras de detecção gerarem falsos positivos/negativos não percebidos por falta de dados de teste representativos | Médio | Média | Simulador cobrindo cenários normais e anômalos (Fase 5); `falsePositiveNotes` obrigatório por regra |
| R-08 | Pipeline de CI com múltiplas verificações (SAST, SCA, secret scanning, SBOM, E2E) tornar o tempo de build excessivo | Médio | Média | Jobs paralelos, gates seletivos por tipo de mudança (Fase 12) |
| R-09 | Manutenção solo do projeto (um único mantenedor) limitar a velocidade de correção de findings de segurança do próprio CI | Médio | Média | Priorização de correções de segurança antes de novas funcionalidades, conforme regra geral do projeto |
| R-10 | Kubernetes educacional (manifests) ficar desatualizado em relação ao Docker Compose por ser um caminho secundário de execução | Baixo | Média | Documentar Kubernetes como educacional/não primário; validar manifests via `kubectl apply --dry-run` na fase correspondente |

## Limitações Conhecidas do MVP (não são pendências de fase)

- Sem integração real com HashiCorp Vault, AWS KMS, Azure Key Vault ou Google Cloud KMS
  no MVP — apenas documentada como roadmap (ver [ADR-004](../adr/ADR-004-tokenizacao.md)).
- Sem machine learning/UEBA estatístico real no MVP — motor de risco é determinístico
  por regras, com interfaces preparadas para uma futura integração.
- Sem múltiplos microsserviços no MVP — arquitetura de monólito modular (ver
  [ADR-001](../adr/ADR-001-monolito-modular.md)).

Essas limitações são intencionais e documentadas como roadmap; não representam tarefas
incompletas de nenhuma fase específica.

## Documentos Relacionados

- [Arquitetura](overview.md)
- [Modelo de Ameaça STRIDE](../threat-model/stride.md)
- [Roadmap](../roadmap.md)
