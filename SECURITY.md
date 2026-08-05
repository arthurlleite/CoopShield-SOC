# Política de Segurança — CoopShield SOC

> Projeto independente, educacional e de portfólio. Não possui vínculo com
> instituições financeiras ou empresas reais. Todos os dados, usuários, eventos,
> transações e incidentes apresentados são sintéticos.

## Natureza do Projeto

O CoopShield SOC é um projeto educacional que processa exclusivamente dados
sintéticos. Não há dados reais de clientes, funcionários, contas ou instituições em
nenhum ambiente do projeto (repositório, demonstração pública ou execução local).

## Relato de Vulnerabilidades

Se você identificar uma vulnerabilidade de segurança neste projeto (por exemplo, uma
falha de autorização, exposição de segredo, injeção, ou qualquer desvio dos controles
descritos em [docs/threat-model/stride.md](docs/threat-model/stride.md)), relate por
meio de uma *issue* privada ou *security advisory* do GitHub no repositório, evitando
detalhar publicamente o passo a passo de exploração antes de uma correção estar
disponível.

Não é necessário relatar problemas relacionados à natureza sintética dos dados (por
definição, não há dado real a proteger), mas é muito bem-vindo relatar qualquer
comportamento que trate o projeto como se processasse dados reais de forma inadequada
(por exemplo, ausência de mascaramento onde o projeto declara que deveria existir).

## Práticas de Segurança Adotadas

- Segurança por design alinhada a OWASP ASVS/OWASP Top 10.
- Autenticação (JWT de curta duração, refresh token) e autorização por perfil (RBAC)
  com menor privilégio.
- Tokenização e mascaramento de dados sensíveis sintéticos antes de qualquer
  persistência ou publicação para análise.
- Logs estruturados sem senhas, tokens, chaves ou dados sensíveis não mascarados.
- Auditoria de ações privilegiadas e de tentativas de destokenização.
- Pipeline de CI com análise estática (CodeQL), análise de dependências, secret
  scanning e geração de SBOM (a partir da Fase 12).
- Gerenciamento de segredos via variáveis de ambiente no ambiente local, com caminho de
  evolução documentado para HashiCorp Vault, AWS KMS, Azure Key Vault ou Google Cloud
  KMS (ver [ADR-004](docs/adr/ADR-004-tokenizacao.md)).

## Escopo desta Política

Esta política cobre o código e a documentação deste repositório. Não cobre nenhum
sistema de terceiros, real ou de produção, pois o projeto não se integra com nenhum.
