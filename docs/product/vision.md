# Visão do Produto — CoopShield SOC

> **Aviso:** Projeto independente, educacional e de portfólio. Não possui vínculo com
> instituições financeiras ou empresas reais. Todos os dados, usuários, eventos,
> transações e incidentes apresentados são sintéticos.

## 1. Visão

CoopShield SOC é uma plataforma educacional de defesa cibernética e proteção de dados
centrada no dado, construída para demonstrar — de ponta a ponta e com profundidade
técnica real — como um Centro de Operações de Segurança (SOC) de um ambiente financeiro
fictício (cooperativa, banco, fintech ou instituição de pagamento sintética) recebe,
analisa, prioriza e responde a eventos de segurança.

O projeto não presta serviço a nenhuma instituição real. Ele existe para permitir que o
autor demonstre, em entrevistas técnicas e no próprio código, competência real em:

- engenharia de software backend e frontend;
- arquitetura de sistemas orientados a eventos;
- práticas de Blue Team e operação de SOC;
- engenharia de detecção (detection engineering);
- proteção de dados centrada no dado (tokenização, mascaramento, criptografia);
- DevSecOps e segurança de aplicações (OWASP ASVS/Top 10);
- observabilidade e resposta a incidentes.

## 2. Problema

Instituições financeiras — reais, mas aqui representadas apenas de forma fictícia e
sintética — processam um volume muito grande de eventos: autenticações, autorizações,
consultas de clientes, exportações de dados, chamadas de API, alterações administrativas
e transações. A maior parte desses eventos é legítima. Uma fração pequena representa
risco real: uma conta comprometida, um abuso de privilégio, uma exportação de dados fora
do padrão, uma sequência de ações administrativas suspeitas.

O desafio central de um SOC não é "ter dados" — é **transformar volume de eventos em
sinal acionável**: reduzir ruído, correlacionar eventos aparentemente inofensivos,
calcular risco de forma explicável, e permitir que analistas investiguem e respondam com
rapidez, sem nunca expor dados sensíveis no processo.

Ao mesmo tempo, qualquer plataforma que processe esse tipo de evento se torna, ela
própria, uma superfície sensível: os logs, alertas e incidentes de segurança podem
conter — ou apontar para — dados pessoais e financeiros. Proteger o dado dentro da
própria ferramenta de segurança é parte do problema, não um adicional.

## 3. Objetivos

1. Demonstrar um pipeline completo de ingestão, normalização, proteção, detecção,
   correlação, priorização por risco, alertas e incidentes.
2. Demonstrar segurança centrada no dado: identificação de dados sensíveis, tokenização,
   mascaramento e controle de destokenização antes que qualquer evento seja persistido
   ou publicado para análise.
3. Demonstrar engenharia de detecção explicável: regras versionadas, testáveis, mapeadas
   ao MITRE ATT&CK, com pontuação de risco determinística e auditável.
4. Demonstrar práticas de Blue Team: investigação, linha do tempo, playbooks defensivos
   simulados, métricas de tempo de detecção/triagem/resposta.
5. Demonstrar maturidade de engenharia de software: arquitetura hexagonal, monólito
   modular orientado a eventos, testes em múltiplas camadas, observabilidade e DevSecOps.
6. Ser executável e demonstrável por qualquer pessoa: localmente via Docker Compose (modo
   completo) e publicamente via GitHub Pages (modo demonstração, somente front-end).
7. Ser tecnicamente defensável em entrevista: cada decisão de arquitetura documentada via
   ADR, cada afirmação sustentada por código e testes reais.

## 4. Escopo

### Dentro do escopo

- Backend Java 21 / Spring Boot como monólito modular orientado a eventos.
- Frontend React/TypeScript com modo de demonstração estático e modo conectado à API.
- Ingestão, normalização, proteção, detecção, correlação, risco, alertas e incidentes
  para eventos **sintéticos** de autenticação, autorização, acesso a dados, API,
  dispositivos, administração e eventos financeiros fictícios.
- Simulador/laboratório de geração de eventos sintéticos com personagens e cenários.
- Proteção de dados sensíveis sintéticos via tokenização e mascaramento.
- Motor de regras de detecção configurável via YAML, mapeado ao MITRE ATT&CK.
- Motor de risco determinístico e explicável.
- Gestão de alertas e incidentes com playbooks defensivos simulados.
- Auditoria de ações sensíveis e de tentativas de destokenização.
- Observabilidade (métricas, health checks, logs estruturados).
- Pipeline de DevSecOps (build, testes, SAST, SCA, secret scanning, SBOM, deploy do
  front-end).
- Documentação completa (arquitetura, modelo de ameaça, catálogos, ADRs, runbooks).

### Fora do escopo

- Qualquer integração com sistemas bancários, cooperativos ou de pagamento reais.
- Qualquer dado real de cliente, funcionário, conta, cartão, CPF, chave PIX ou documento.
- Machine learning / UEBA estatístico real no MVP (apenas interfaces preparadas para
  uma futura integração — ver [Roadmap](../roadmap.md)).
- Múltiplos microsserviços no MVP (o projeto começa como monólito modular; a extração de
  módulos para microsserviços é um item de roadmap futuro, não do MVP).
- Integração com provedores reais de KMS/Vault em produção (documentado como roadmap;
  o MVP usa segredos locais via variáveis de ambiente).
- Aplicativo móvel nativo.
- Multi-tenência real ou suporte a múltiplos clientes pagantes.
- Qualquer ação de contenção real sobre contas, dispositivos ou sistemas de terceiros —
  todas as ações de resposta são **simuladas**.

## 5. Requisitos Funcionais (RF)

| ID | Requisito |
|----|-----------|
| RF-01 | O sistema deve receber eventos sintéticos de autenticação, autorização, acesso a dados, API, dispositivos, administração e eventos financeiros fictícios. |
| RF-02 | O sistema deve publicar e consumir eventos via Apache Kafka no modo completo. |
| RF-03 | O sistema deve validar e normalizar eventos recebidos para um modelo comum. |
| RF-04 | O sistema deve identificar campos sensíveis nos eventos antes de qualquer persistência ou publicação para análise. |
| RF-05 | O sistema deve tokenizar dados sensíveis antes da persistência normalizada e da publicação em tópicos de análise. |
| RF-06 | O sistema deve mascarar dados sensíveis na interface e impedir sua exposição em logs e mensagens de erro. |
| RF-07 | O sistema deve exigir autorização e justificativa para destokenização, limitada por perfil, com auditoria. |
| RF-08 | O sistema deve aplicar regras de detecção configuráveis (YAML) sobre os eventos normalizados. |
| RF-09 | O sistema deve calcular uma pontuação de risco determinística e explicável (0–100) para cada alerta. |
| RF-10 | O sistema deve correlacionar eventos relacionados via correlation ID e janelas de agregação. |
| RF-11 | O sistema deve criar alertas quando uma regra for acionada, com evidências e fatores de risco explícitos. |
| RF-12 | O sistema deve permitir promover um ou mais alertas a um incidente. |
| RF-13 | O sistema deve permitir investigação de incidentes: evidências, linha do tempo, notas de analista, playbook, contenção simulada, conclusão. |
| RF-14 | O sistema deve mapear cada regra de detecção acionada a uma tática e técnica MITRE ATT&CK. |
| RF-15 | O sistema deve recomendar playbooks defensivos simulados por tipo de cenário. |
| RF-16 | O sistema deve oferecer um dashboard com métricas operacionais de SOC (alertas ativos, incidentes críticos, MTTD/MTTA/MTTR simulados, etc.). |
| RF-17 | O sistema deve oferecer um laboratório de simulação com personagens e cenários sintéticos configuráveis. |
| RF-18 | O sistema deve autenticar usuários e autorizar ações por perfil (RBAC) com os perfis definidos na seção 22. |
| RF-19 | O sistema deve registrar auditoria de autenticações, falhas, ações privilegiadas e tentativas de destokenização. |
| RF-20 | O sistema deve executar localmente via Docker Compose (modo completo) e publicamente via GitHub Pages (modo demonstração). |

## 6. Requisitos Não Funcionais (RNF)

| ID | Requisito |
|----|-----------|
| RNF-01 | Segurança por design, alinhada a OWASP ASVS e OWASP Top 10. |
| RNF-02 | Nenhum segredo, senha, token, chave ou dado sensível em texto puro deve aparecer em logs, respostas de erro ou repositório. |
| RNF-03 | Toda decisão de risco e toda regra de detecção deve ser explicável e determinística (auditável por um humano). |
| RNF-04 | O sistema deve ser observável: métricas, health checks, logs estruturados com correlation ID. |
| RNF-05 | O backend deve seguir arquitetura hexagonal e princípios de DDD, com módulos de baixo acoplamento e alta coesão. |
| RNF-06 | O frontend deve ser responsivo, acessível (navegação por teclado, contraste, leitura por leitor de tela) e suportar modo claro/escuro. |
| RNF-07 | O sistema deve ser executável localmente por qualquer pessoa com Docker instalado, com dados sintéticos de inicialização. |
| RNF-08 | O modo de demonstração (GitHub Pages) não deve depender de backend nem exigir segredos. |
| RNF-09 | O pipeline de CI deve falhar diante de build quebrado, teste obrigatório falho, vulnerabilidade crítica ou segredo detectado. |
| RNF-10 | O código e a documentação devem permanecer consistentes entre si a cada fase entregue. |
| RNF-11 | Processamento de eventos deve ser idempotente e tolerante a duplicidade. |
| RNF-12 | O sistema deve oferecer testes automatizados relevantes nas camadas de domínio, segurança, regras, risco, tokenização, autorização, incidentes e auditoria. |

## 7. Aviso de Independência e Dados Sintéticos

Este projeto é independente e educacional. Não possui vínculo com bancos,
cooperativas, empresas de segurança ou instituições financeiras reais. Não utiliza
logotipos, identidade visual proprietária, nomes de
clientes ou funcionários reais, dados bancários reais ou qualquer informação
confidencial. Todos os dados, usuários, eventos, transações e incidentes apresentados
neste sistema são sintéticos e claramente identificados como fictícios em toda a
interface e documentação.

## Documentos Relacionados

- [Personas e Casos de Uso](personas-use-cases.md)
- [Modelo de Ameaça STRIDE](../threat-model/stride.md)
- [Arquitetura](../architecture/overview.md)
- [Roadmap](../roadmap.md)
