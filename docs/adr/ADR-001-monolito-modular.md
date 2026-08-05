# ADR-001: Uso de Monólito Modular como Arquitetura Inicial

- **Status:** Aceito
- **Data:** 2026-08-05

## Contexto

O CoopShield SOC precisa de uma arquitetura que suporte múltiplos domínios (identidade,
ingestão, normalização, proteção de dados, detecção, risco, alertas, incidentes,
playbooks, auditoria, observabilidade, simulação) com baixo acoplamento entre eles, sem
introduzir a complexidade operacional de múltiplos microsserviços em um projeto de
portfólio mantido por uma pessoa.

## Decisão

Adotar um **monólito modular orientado a eventos** como arquitetura do MVP. Cada domínio
é implementado como um módulo com limites claros (ver
[Arquitetura](../architecture/overview.md)), comunicando-se por interfaces explícitas e
eventos (internos e via Kafka), nunca por acesso direto a dados de outro módulo.

## Alternativas Consideradas

- **Microsserviços desde o início:** rejeitada para o MVP. Aumentaria drasticamente a
  complexidade operacional (múltiplos deploys, service discovery, observabilidade
  distribuída) sem benefício proporcional para um projeto de portfólio.
- **Monólito não modular (camadas técnicas apenas):** rejeitada por dificultar a
  demonstração de limites de domínio e DDD, e por acoplar módulos que precisam evoluir
  de forma independente.

## Consequências

- Um único processo, um único deploy, um único banco de dados no MVP — simplicidade
  operacional.
- Limites de módulo bem definidos permitem, no futuro, extrair um módulo (por exemplo,
  `dataprotection` ou `detection`) para um serviço independente sem reescrever a lógica
  de domínio, desde que a comunicação já ocorra por portas/eventos.
- Exige disciplina para não permitir acoplamento indevido entre módulos (dependências
  circulares, acesso direto a repositório de outro módulo).

## Referências

- [Arquitetura](../architecture/overview.md)
- [ADR-009: Arquitetura Hexagonal](ADR-009-arquitetura-hexagonal.md)
