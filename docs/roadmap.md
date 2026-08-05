# Roadmap — CoopShield SOC

> As fases são executadas sequencialmente. Nenhuma fase é iniciada sem autorização
> explícita, e nenhuma fase é considerada concluída sem atender integralmente aos seus
> critérios de conclusão (ver seção "Definição de Fase 100% Concluída" combinada com o
> checklist específico de cada fase).

## Fases

| Fase | Nome | Escopo Principal |
|------|------|-------------------|
| 0 | Arquitetura e documentação inicial | Visão de produto, modelo de ameaça, arquitetura, catálogos iniciais, ADRs, roadmap, estrutura do repositório |
| 1 | Estrutura do back-end e front-end | Esqueleto Maven/Spring Boot modular, esqueleto React/Vite/TypeScript, Docker Compose inicial, CI mínimo de build |
| 2 | Autenticação e autorização | Módulos `identity` e `accesscontrol`: JWT, refresh token, RBAC por perfil, bloqueio temporário, auditoria de autenticação |
| 3 | MongoDB e modelos de domínio | Coleções, índices, TTL, modelos de domínio das entidades principais |
| 4 | Kafka e ingestão de eventos | Tópicos, produtores/consumidores, idempotência, dead-letter, módulos `eventingestion`/`eventnormalization` |
| 5 | Simulador e dados sintéticos | Módulo `simulation`, personagens, cenários, geração de eventos sintéticos |
| 6 | Motor de detecção | Módulo `detection`, carregador de regras YAML, avaliação das 15 regras iniciais, testes por regra |
| 7 | Motor de risco | Módulo `risk`, cálculo determinístico e explicável de pontuação de risco |
| 8 | Alertas e incidentes | Módulos `alert`, `incident`, `playbook`: ciclo de vida completo, playbooks defensivos simulados |
| 9 | Proteção de dados | Módulo `dataprotection`: identificação, tokenização, mascaramento, controle de destokenização, auditoria |
| 10 | Dashboard e laboratório visual | Páginas de frontend: Dashboard SOC, Laboratory, integração visual completa |
| 11 | Observabilidade | Actuator, Micrometer, Prometheus, Grafana, OpenTelemetry, métricas definidas na especificação |
| 12 | Testes e DevSecOps | Cobertura de testes back-end/front-end/carga, pipeline completo de GitHub Actions |
| 13 | GitHub Pages e modo demonstração | Publicação do front-end estático, DEMO_MODE completo, validação de base path/roteamento |
| 14 | Documentação, validação e entrega final | Consolidação de toda a documentação, validação end-to-end, revisão final de segurança |

## Critérios Gerais de Conclusão de Fase (aplicam-se a todas as fases)

Uma fase só é considerada concluída quando, cumulativamente:

1. todos os itens previstos para a fase estão implementados e verificados;
2. build e testes aplicáveis passam;
3. não há TODOs, FIXMEs, placeholders, pseudocódigo ou implementação parcial;
4. a documentação relacionada está atualizada e compatível com o código;
5. os commits foram realizados com Arthur Carvalho Leite como único autor;
6. os commits foram publicados no repositório remoto e o branch local está
   sincronizado com o remoto;
7. um relatório de fase foi apresentado, seguindo o formato definido nas regras gerais
   de execução.

Limitações planejadas (ex.: ausência de integração real com Vault/KMS, ausência de
UEBA/ML) são registradas como roadmap futuro e não podem ser usadas para justificar
tarefa incompleta da fase atual — ver [Riscos Técnicos](architecture/technical-risks.md).

## Critérios Específicos por Fase (visão inicial — refinados no início de cada fase)

- **Fase 1:** projeto Maven compila; projeto Vite builda; `docker compose up` inicia
  backend, frontend, MongoDB e Kafka com health checks passando; CI executa build de
  ambos.
- **Fase 2:** login/logout funcionam; RBAC testado (acesso permitido/negado) para todos
  os perfis; bloqueio temporário testado; nenhuma senha/token em log.
- **Fase 3:** todas as coleções e índices criados; testes de persistência via
  Testcontainers; estratégia de TTL/retenção documentada e implementada onde aplicável.
- **Fase 4:** todos os tópicos criados; produtores/consumidores testados com
  Testcontainers; idempotência e dead-letter testados.
- **Fase 5:** simulador gera eventos para todos os cenários e personagens descritos na
  visão do produto; eventos publicados no Kafka no modo completo.
- **Fase 6:** as 15 regras iniciais implementadas, testadas e carregadas de YAML;
  cada alerta gerado explica regra, evidências, limite e MITRE.
- **Fase 7:** motor de risco determinístico, testado, com fatores de risco explícitos
  na resposta da API.
- **Fase 8:** ciclo de vida completo de alerta e incidente testado (todas as
  transições de estado); playbooks executáveis (simulados).
- **Fase 9:** tokenização/mascaramento testados; destokenização exige autorização e
  justificativa; testes de ausência de dado sensível em log.
- **Fase 10:** dashboard e laboratório funcionais em Live Mode, com dados reais do
  backend.
- **Fase 11:** métricas expostas e coletadas por Prometheus; dashboards Grafana
  provisionados; health/readiness/liveness funcionando.
- **Fase 12:** pipeline de CI completo, com todos os gates de segurança definidos na
  especificação; metas mínimas de cobertura atingidas nas áreas prioritárias.
- **Fase 13:** GitHub Pages publicado, DEMO_MODE navegável de ponta a ponta sem
  backend.
- **Fase 14:** toda a documentação revisada e consistente; validação end-to-end final;
  relatório de entrega.

## Documentos Relacionados

- [Planejamento da Fase 1](phase-1-plan.md)
- [Checklist de Conclusão da Fase 0](phase-0-completion-checklist.md)
- [Riscos Técnicos](architecture/technical-risks.md)
