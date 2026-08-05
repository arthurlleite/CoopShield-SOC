# ADR-009: Arquitetura Hexagonal e DDD nos Módulos do Backend

- **Status:** Aceito
- **Data:** 2026-08-05

## Contexto

Cada módulo do monólito (ver [ADR-001](ADR-001-monolito-modular.md)) precisa manter seu
domínio isolado de detalhes de infraestrutura (REST, Kafka, MongoDB), para permitir
testes de domínio rápidos e independentes, e para viabilizar uma futura extração de
módulo para microsserviço sem reescrever a lógica de negócio.

## Decisão

Estruturar cada módulo internamente segundo **arquitetura hexagonal** (portas e
adaptadores), com camadas:

- `domain`: entidades, value objects, regras de negócio; sem dependência de Spring,
  Kafka client ou driver MongoDB.
- `application`: casos de uso, portas de entrada (use cases) e portas de saída
  (interfaces implementadas pela infraestrutura).
- `infrastructure`: adaptadores concretos — controladores REST, produtores/consumidores
  Kafka, repositórios MongoDB.

Princípios de DDD aplicados: linguagem ubíqua por módulo, agregados com fronteiras
claras, eventos de domínio, e separação entre domínio e infraestrutura (inversão de
dependência).

## Alternativas Consideradas

- **Arquitetura em camadas técnicas tradicionais (Controller/Service/Repository sem
  isolamento de domínio):** rejeitada por acoplar regra de negócio a frameworks,
  dificultando testes unitários de domínio e a demonstração de DDD/Clean Architecture
  como competência.

## Consequências

- Maior quantidade inicial de interfaces/classes por módulo, compensada por testabilidade
  e clareza de fronteiras.
- Testes de domínio não exigem Spring context nem containers, sendo rápidos e
  determinísticos.
- Requer disciplina de revisão para não permitir "vazamento" de anotações Spring ou
  tipos de driver para dentro do pacote `domain`.

## Referências

- [ADR-001: Monólito Modular](ADR-001-monolito-modular.md)
- [Arquitetura](../architecture/overview.md)
