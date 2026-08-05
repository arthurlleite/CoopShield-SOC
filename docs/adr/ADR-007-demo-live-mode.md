# ADR-007: Separação entre Demo Mode e Live Mode

- **Status:** Aceito
- **Data:** 2026-08-05

## Contexto

O front-end precisa funcionar em dois contextos muito diferentes: publicado
estaticamente no GitHub Pages (sem backend disponível) e executado localmente contra o
backend real via Docker Compose. Ambos os contextos devem compartilhar a mesma base de
código de UI, para evitar duplicidade e divergência visual/funcional.

## Decisão

Implementar uma camada de acesso a dados no front-end com duas implementações da mesma
interface:

- **DEMO_MODE:** lê arquivos JSON sintéticos estáticos (cenários, eventos, alertas,
  incidentes pré-computados), executa a lógica de simulação inteiramente no navegador,
  não exige autenticação real e deixa explícito na interface que se trata de uma
  demonstração.
- **LIVE_MODE:** consome a API REST do backend Spring Boot, com autenticação JWT real,
  eventos publicados/consumidos via Kafka e persistidos em MongoDB.

O modo é selecionado por variável de ambiente de build do front-end, nunca por dado
sensível ou segredo.

## Alternativas Consideradas

- **Duas bases de código de front-end separadas (uma para demo, outra para live):**
  rejeitada por gerar duplicação de UI e risco de divergência entre o que é
  demonstrado publicamente e o que de fato existe no sistema completo.
- **Backend real hospedado publicamente para a demonstração:** rejeitado no MVP por
  custo e superfície de exposição desnecessária para um projeto de portfólio (ver
  [ADR-005](ADR-005-github-pages-demo.md)).

## Consequências

- É necessário definir uma interface comum de acesso a dados (ex.: `DataGateway`) com
  duas implementações, mantendo os componentes de UI agnósticos ao modo.
- É necessário garantir que nenhuma implementação de `DEMO_MODE` dependa de rede ou
  segredo.
- Testes de front-end devem cobrir ambos os modos onde aplicável.

## Referências

- [ADR-005: GitHub Pages em Modo Demonstrativo](ADR-005-github-pages-demo.md)
- [Arquitetura](../architecture/overview.md)
