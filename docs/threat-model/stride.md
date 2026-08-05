# Modelo de Ameaça (STRIDE) — CoopShield SOC

> **Aviso:** Este modelo de ameaça descreve um sistema educacional que processa
> exclusivamente dados sintéticos. Os riscos aqui listados são tratados com o mesmo
> rigor de um ambiente real, como exercício de engenharia de segurança, mas nenhum
> ativo real está envolvido.

## 1. Ativos Protegidos

| Ativo | Descrição |
|-------|-----------|
| A-01 | Dados sensíveis sintéticos (CPF fictício, conta fictícia, telefone, e-mail, chave PIX fictícia, cartão sintético, identificador de cliente/cooperado, documentos fictícios) |
| A-02 | Credenciais e sessões (JWT, refresh tokens, hashes de senha) |
| A-03 | Chaves de tokenização/criptografia e segredos de configuração |
| A-04 | Regras de detecção (YAML) e sua integridade/versão |
| A-05 | Eventos de segurança normalizados e sua trilha de auditoria |
| A-06 | Alertas e incidentes (evidências, notas de investigação, conclusões) |
| A-07 | Disponibilidade da pipeline de ingestão e detecção |
| A-08 | Integridade do pipeline de CI/CD e dos artefatos publicados |
| A-09 | Interface de administração e endpoints privilegiados |

## 2. Agentes de Ameaça

| Agente | Motivação / Capacidade |
|--------|--------------------------|
| T-01 | Usuário externo não autenticado tentando acessar endpoints protegidos |
| T-02 | Usuário autenticado tentando escalar privilégios ou acessar dados fora do seu perfil |
| T-03 | Insider malicioso (perfil legítimo abusando de acesso, ex.: exportação massiva) |
| T-04 | Atacante explorando dependências vulneráveis (supply chain) |
| T-05 | Atacante tentando extrair segredos do repositório, imagem de container ou logs |
| T-06 | Atacante tentando injetar payloads maliciosos via API (injeção, mass assignment) |
| T-07 | Atacante tentando interceptar ou repetir eventos Kafka (replay, duplicidade) |
| T-08 | Agente automatizado realizando força bruta de autenticação |

## 3. Superfícies de Ataque

- API REST do backend (autenticação, endpoints de alertas/incidentes/regras/dados).
- Tópicos Kafka (produção e consumo de eventos).
- Frontend React (modo Live conectado à API; modo Demo estático no GitHub Pages).
- Pipeline de CI/CD (GitHub Actions) e artefatos gerados (imagens Docker, SBOM).
- Configuração de infraestrutura local (Docker Compose, variáveis de ambiente).
- Logs e métricas expostos via Actuator/Prometheus.

## 4. Análise STRIDE por Componente

### 4.1 Identidade e Controle de Acesso

| Categoria | Ameaça | Controle Proposto |
|-----------|--------|--------------------|
| Spoofing | Falsificação de identidade via credenciais fracas ou força bruta | Política de senha, bloqueio temporário após tentativas repetidas, JWT de curta duração |
| Tampering | Manipulação de token JWT | Assinatura e validação de JWT, verificação de expiração e escopo |
| Repudiation | Usuário nega ter realizado ação privilegiada | Auditoria de autenticações, falhas e ações privilegiadas com actor e correlationId |
| Information Disclosure | Enumeração de usuários via mensagens de erro distintas | Mensagens de erro genéricas para login/autorização, sem diferenciar "usuário não existe" de "senha incorreta" |
| Denial of Service | Força bruta esgotando recursos de autenticação | Rate limiting, bloqueio temporário |
| Elevation of Privilege | Escalonamento de perfil (ex.: EMPLOYEE acessando endpoint de IT_ADMIN) | Autorização por perfil (RBAC) em todos os endpoints, testes de acesso permitido/negado |

### 4.2 Ingestão e Processamento de Eventos

| Categoria | Ameaça | Controle Proposto |
|-----------|--------|--------------------|
| Spoofing | Evento forjado se passando por fonte legítima | Validação de schema, autenticação de produtores internos |
| Tampering | Alteração de evento já persistido | Eventos imutáveis após persistência; qualquer correção gera novo evento versionado |
| Repudiation | Ausência de rastreabilidade de origem do evento | correlationId, eventId, eventVersion obrigatórios em todo evento |
| Information Disclosure | Dado sensível em texto puro em tópico de análise | Tokenização antes da publicação em tópicos de análise; alerta automático se dado sensível for detectado em texto puro |
| Denial of Service | Inundação de eventos malformados | Validação na borda, dead-letter topic, retry com backoff |
| Elevation of Privilege | Consumo indevido de tópico restrito | Segmentação de tópicos e controle de acesso ao cluster Kafka |

### 4.3 Proteção de Dados (Tokenização/Mascaramento)

| Categoria | Ameaça | Controle Proposto |
|-----------|--------|--------------------|
| Spoofing | Solicitação de destokenização se passando por perfil autorizado | Autorização por perfil + autenticação obrigatória para destokenizar |
| Tampering | Substituição de token por valor arbitrário | Armazenamento de token e valor protegido separados, com verificação de integridade |
| Repudiation | Destokenização sem justificativa registrada | Justificativa obrigatória e auditoria de toda tentativa de destokenização |
| Information Disclosure | Vazamento de dado sensível em log ou mensagem de erro | Filtros de log, testes automatizados de ausência de dado sensível em logs |
| Denial of Service | Uso indevido do serviço de destokenização para exaurir recursos | Rate limiting e limite de destokenizações por perfil/período |
| Elevation of Privilege | Perfil sem permissão obtendo valor protegido | Verificação de perfil autorizado antes de qualquer destokenização |

### 4.4 Motor de Detecção e Risco

| Categoria | Ameaça | Controle Proposto |
|-----------|--------|--------------------|
| Tampering | Alteração não autorizada de regra YAML para reduzir sensibilidade | Regras versionadas em controle de código, revisão obrigatória, autoria e data registradas |
| Repudiation | Alerta gerado sem explicação de como o risco foi calculado | Fatores de risco explícitos e persistidos junto ao alerta |
| Information Disclosure | Evidência de alerta expondo dado sensível não tokenizado | Uso de referências a eventIds tokenizados, nunca do dado bruto |
| Denial of Service | Volume de eventos correlacionados degradando o motor de detecção | Janelas de agregação limitadas, métricas de tempo de processamento |

### 4.5 Pipeline de CI/CD e Supply Chain

| Categoria | Ameaça | Controle Proposto |
|-----------|--------|--------------------|
| Tampering | Dependência comprometida introduzindo código malicioso | Análise de dependências (SCA), geração de SBOM |
| Information Disclosure | Segredo commitado acidentalmente | Secret scanning obrigatório no pipeline |
| Elevation of Privilege | Workflow do GitHub Actions com permissões excessivas | Permissões mínimas por workflow, revisão de `permissions:` em cada job |
| Denial of Service | Pipeline sem limite travando recursos de CI | Timeouts definidos por job |

## 5. Controles Transversais Propostos

- Validação de entrada em todas as bordas (API, consumidores Kafka).
- Autenticação e autorização (RBAC) por perfil em todos os endpoints.
- Rate limiting em endpoints de autenticação e destokenização.
- CORS restrito e headers de segurança (CSP, HSTS quando aplicável, X-Content-Type-Options).
- Logs estruturados sem dados sensíveis, com correlationId.
- Auditoria de ações privilegiadas e tentativas de destokenização.
- Gerenciamento de segredos via variáveis de ambiente localmente, com caminho de
  evolução documentado para Vault/KMS gerenciado.
- Análise estática (CodeQL), análise de dependências e secret scanning no CI.
- Geração de SBOM para os artefatos de container.

## Documentos Relacionados

- [Visão do Produto](../product/vision.md)
- [Arquitetura](../architecture/overview.md)
- [Perfis e Permissões](../architecture/roles-permissions.md)
