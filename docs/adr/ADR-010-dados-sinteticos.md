# ADR-010: Dados Exclusivamente Sintéticos

- **Status:** Aceito
- **Data:** 2026-08-05

## Contexto

O projeto simula um ambiente financeiro (cooperativa/banco/fintech) para fins
educacionais e de portfólio. Existe risco relevante — legal, ético e reputacional — em
usar, mesmo que de forma ilustrativa, dados reais ou identificáveis de qualquer
instituição, cliente ou funcionário.

## Decisão

Todos os dados do sistema — usuários, contas, documentos, chaves PIX, cartões,
dispositivos, transações, eventos e incidentes — são **exclusivamente sintéticos**,
gerados por geradores determinísticos ou pelo simulador do próprio projeto. Nenhum
logotipo, identidade visual proprietária ou nome de instituição/cliente/funcionário
real é utilizado. A interface e a documentação exibem, de forma permanente, o aviso:

> "Projeto independente, educacional e de portfólio. Não possui vínculo com
> instituições financeiras ou empresas reais. Todos os dados, usuários, eventos,
> transações e incidentes apresentados são sintéticos."

## Alternativas Consideradas

- **Uso de datasets públicos anonimizados de instituições reais:** rejeitada pelo risco
  de reidentificação e por criar a impressão de vínculo com uma instituição real, o que
  contraria diretamente o objetivo de independência do projeto.
- **Uso de nomes/marcas de instituições reais apenas como "inspiração declarada" na
  interface:** rejeitada; a inspiração conceitual é documentada apenas em texto
  descritivo (ex.: "inspirado em desafios de SOCs financeiros"), nunca com uso de
  marca, logotipo ou identidade visual de terceiros.

## Consequências

- Todos os geradores de dados (simulador, massa inicial do banco) devem produzir dados
  sintéticos claramente identificáveis como tais (ex.: prefixo `synthetic-`).
- Testes e demonstrações nunca dependem de dados externos reais.
- Facilita a publicação pública do projeto (GitHub, GitHub Pages) sem risco de exposição
  de dado real.

## Referências

- [Visão do Produto](../product/vision.md)
- [Modelo de Ameaça STRIDE](../threat-model/stride.md)
