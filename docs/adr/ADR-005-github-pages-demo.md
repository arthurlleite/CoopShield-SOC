# ADR-005: GitHub Pages em Modo Demonstrativo

- **Status:** Aceito
- **Data:** 2026-08-05

## Contexto

O projeto precisa ser demonstrável publicamente (por exemplo, a recrutadores) sem exigir
que quem avalia suba a stack completa (backend, Kafka, MongoDB) localmente. Ao mesmo
tempo, o projeto também deve poder ser executado por completo localmente para
demonstrar profundidade técnica de backend.

## Decisão

Publicar o **front-end estático** no GitHub Pages, operando em `DEMO_MODE`: consumindo
arquivos JSON sintéticos versionados no repositório, sem depender de backend, Kafka ou
MongoDB, sem autenticação real e sem qualquer segredo embarcado. A execução local via
Docker Compose usa `LIVE_MODE`, conectando o front-end à API Spring Boot real.

## Alternativas Consideradas

- **Publicar apenas instruções de execução local, sem demonstração pública:**
  rejeitada por reduzir o alcance do projeto como peça de portfólio — a maioria dos
  avaliadores não vai subir a stack completa apenas para conhecer o projeto.
  Publicar somente uma demo ao vivo hospedada (backend real): rejeitado no MVP por
  custo/complexidade de manter infraestrutura paga rodando publicamente.

## Consequências

- É necessário manter dois modos de operação no front-end (`DEMO_MODE`/`LIVE_MODE`),
  com uma camada de abstração de acesso a dados que alterna entre arquivos JSON e a API
  real (ver [ADR-007](ADR-007-demo-live-mode.md)).
- É necessário configurar corretamente o `base` do Vite e o roteamento do React Router
  para funcionar sob o caminho de projeto do GitHub Pages.
- Nenhum segredo pode ser incluído no build publicado — reforça a necessidade de
  revisão do pipeline de deploy.

## Referências

- [ADR-007: Separação entre Demo Mode e Live Mode](ADR-007-demo-live-mode.md)
- [Visão do Produto](../product/vision.md)
