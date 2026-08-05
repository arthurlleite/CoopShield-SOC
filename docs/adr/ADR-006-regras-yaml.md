# ADR-006: Regras de Detecção Explicáveis em YAML

- **Status:** Aceito
- **Data:** 2026-08-05

## Contexto

O motor de detecção precisa ser explicável e auditável: analistas e avaliadores devem
poder ler exatamente o que cada regra verifica, sem precisar interpretar código
compilado. As regras também precisam evoluir (novas versões, novos limites) sem exigir
recompilação do backend a cada ajuste.

## Decisão

Definir as regras de detecção em **arquivos YAML versionados** no repositório (pasta
`detection-rules/`), seguindo o schema documentado em
[Catálogo de Regras](../detection-rules/catalog.md): identificação, descrição,
tipos de evento, condições, janela de agregação, limite, severidade, score base de
risco, mapeamento MITRE ATT&CK, playbook recomendado, versão, notas de falso positivo,
referências, autor e datas.

## Alternativas Consideradas

- **Regras hardcoded em código Java:** rejeitada por dificultar a leitura/auditoria por
  não desenvolvedores (ex.: analista de SOC, auditor) e por acoplar mudança de regra a
  deploy de código.
- **Motor de regras de terceiros (ex.: Drools):** rejeitada no MVP por adicionar
  complexidade desproporcional ao conjunto de regras inicial (15 regras), que não exige
  um motor de regras genérico complexo.

## Consequências

- É necessário um carregador de regras YAML com validação de schema na inicialização.
- Alterações de regra ficam sujeitas a revisão de código (pull request), o que também
  serve como controle contra alteração maliciosa/acidental de regra (ver Modelo de
  Ameaça STRIDE, seção "Motor de Detecção e Risco").
- Cada regra deve ter teste automatizado correspondente na Fase 6.

## Referências

- [Catálogo de Regras de Detecção](../detection-rules/catalog.md)
- [Modelo de Ameaça STRIDE](../threat-model/stride.md)
