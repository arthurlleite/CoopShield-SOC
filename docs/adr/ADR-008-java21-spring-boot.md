# ADR-008: Java 21 e Spring Boot como Base do Backend

- **Status:** Aceito
- **Data:** 2026-08-05

## Contexto

O backend precisa de uma stack madura, amplamente adotada em ambientes financeiros
reais (o que o projeto busca refletir de forma fictícia), com suporte robusto a
segurança (Spring Security), integração com Kafka e MongoDB, observabilidade
(Actuator/Micrometer) e um ecossistema de testes consolidado.

## Decisão

Utilizar **Java 21** (LTS) com **Spring Boot**, incluindo Spring Security, Spring Data
MongoDB, Spring for Apache Kafka, Spring Boot Actuator, Bean Validation, Maven como
gerenciador de build, e geração de documentação OpenAPI/Swagger.

## Alternativas Consideradas

- **Kotlin + Spring Boot:** viável tecnicamente, mas rejeitada para manter o projeto
  alinhado ao requisito explícito de demonstrar competência em Java, que é o critério
  de avaliação primário para as vagas que o projeto visa suportar.
- **Node.js/NestJS para o backend:** rejeitada pelo mesmo motivo — o objetivo do
  portfólio é demonstrar profundidade em Java/Spring, não em uma stack JavaScript
  full-stack.

## Consequências

- Java 21 permite uso de recursos modernos da linguagem (records, pattern matching,
  virtual threads quando aplicável) nos módulos de domínio.
- Maven é usado como build tool único do backend, com plugins de qualidade (lint,
  cobertura, SBOM) integrados ao pipeline de CI.

## Referências

- [Arquitetura](../architecture/overview.md)
- [ADR-009: Arquitetura Hexagonal](ADR-009-arquitetura-hexagonal.md)
