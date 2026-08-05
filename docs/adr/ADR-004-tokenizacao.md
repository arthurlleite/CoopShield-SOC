# ADR-004: Tokenização Antes da Persistência e Publicação

- **Status:** Aceito
- **Data:** 2026-08-05

## Contexto

Eventos sintéticos podem conter campos considerados sensíveis (CPF fictício, conta
fictícia, telefone, e-mail, chave PIX fictícia, cartão sintético, identificadores de
cliente/cooperado, documentos fictícios). Mesmo sendo dados sintéticos, o projeto deve
demonstrar segurança centrada no dado como se fossem dados reais — essa é justamente a
competência que o projeto pretende evidenciar.

## Decisão

O módulo `dataprotection` identifica e **tokeniza** campos sensíveis **antes** de:

1. qualquer persistência do evento normalizado em MongoDB;
2. qualquer publicação em tópicos Kafka de análise (`security.normalized-events` em
   diante).

Tokens são valores aleatórios criptograficamente seguros, armazenados separadamente do
valor protegido (que é cifrado com AES-GCM). Nenhum módulo além de `dataprotection`
acessa o valor original; destokenização exige autorização por perfil, justificativa e
gera registro de auditoria.

## Alternativas Consideradas

- **Mascaramento apenas na camada de apresentação (sem tokenização):** rejeitada, pois
  deixaria o dado sensível em texto puro em eventos persistidos e em tópicos Kafka,
  falhando o objetivo central de segurança centrada no dado.
- **Criptografia direta do campo, sem tokenização:** rejeitada como abordagem única,
  pois tokens permitem revogar/rotacionar o vínculo sem expor diretamente o valor
  cifrado em cada consumidor; a criptografia (AES-GCM) é usada para proteger o valor
  por trás do token, não como substituto da tokenização.

## Consequências

- Todo consumidor downstream (detecção, risco, alertas, incidentes, auditoria) opera
  apenas sobre tokens/dados mascarados, nunca sobre o valor original.
- É necessário um serviço interno de tokenização/destokenização com chaves geridas fora
  do código-fonte (variáveis de ambiente localmente; caminho de evolução para
  Vault/KMS documentado, mas fora do escopo obrigatório do MVP).
- Nenhum algoritmo criptográfico proprietário é implementado; usam-se bibliotecas e
  algoritmos consolidados (AES-GCM, geradores criptograficamente seguros).

## Referências

- [Modelo de Ameaça STRIDE](../threat-model/stride.md)
- [Visão do Produto](../product/vision.md)
