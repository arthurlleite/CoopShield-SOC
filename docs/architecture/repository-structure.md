# Estrutura do Repositório — CoopShield SOC

## 1. Estrutura Alvo

A estrutura abaixo é a estrutura alvo do repositório ao final das 15 fases (0 a 14).
Nesta Fase 0, apenas os diretórios de documentação e os arquivos de governança do
repositório existem; os demais diretórios (`backend/`, `frontend/`, `infrastructure/`,
`load-tests/`, `.github/workflows/`) serão criados nas fases correspondentes, conforme
o [Roadmap](../roadmap.md).

> **Atualização (Fases 5-6):** o simulador e as regras de detecção, originalmente
> desenhados aqui como diretórios de topo (`simulator/`, `detection-rules/`), foram
> implementados como o módulo `backend/simulation` e como recursos de
> `backend/detection/src/main/resources/detection-rules/`, respectivamente — mantendo
> a arquitetura de monólito modular já decidida em [ADR-001](../adr/ADR-001-monolito-modular.md)
> e, no caso das regras, garantindo que os arquivos YAML estejam dentro do contexto de
> build da imagem Docker do backend (ver [ADR-014](../adr/ADR-014-motor-de-deteccao.md)).
> Ambos continuam "versionados no repositório"; apenas o caminho mudou.

```
coopshield-soc/
├── backend/                     (Fase 1+)
│   ├── src/
│   ├── pom.xml
│   ├── Dockerfile
│   └── README.md
├── frontend/                    (Fase 1+)
│   ├── src/
│   ├── public/
│   ├── package.json
│   ├── vite.config.ts
│   └── README.md
├── infrastructure/               (Fase 1+ / Fase 11+)
│   ├── docker-compose.yml
│   ├── prometheus/
│   ├── grafana/
│   └── kubernetes/
├── docs/                         (Fase 0 — criado nesta fase)
│   ├── product/
│   │   ├── vision.md
│   │   └── personas-use-cases.md
│   ├── architecture/
│   │   ├── overview.md
│   │   ├── roles-permissions.md
│   │   ├── repository-structure.md
│   │   └── technical-risks.md
│   ├── threat-model/
│   │   └── stride.md
│   ├── event-catalog/
│   │   └── events.md
│   ├── detection-rules/
│   │   └── catalog.md
│   ├── adr/
│   │   └── ADR-001 a ADR-010
│   ├── api/                      (Fase 1+ — OpenAPI)
│   ├── incidents/                (Fase 8+)
│   ├── runbooks/                 (Fase 8+)
│   ├── roadmap.md
│   ├── phase-0-completion-checklist.md
│   └── phase-1-plan.md
├── load-tests/                   (Fase 12+)
├── .github/
│   ├── workflows/                (Fase 12+)
│   ├── ISSUE_TEMPLATE/           (Fase 14)
│   └── pull_request_template.md  (Fase 14)
├── LICENSE
├── SECURITY.md
├── CONTRIBUTING.md
├── CODE_OF_CONDUCT.md
└── README.md
```

## 2. Justificativa de Ordem de Criação

A estrutura segue a ordem das fases do [Roadmap](../roadmap.md): documentação e
governança primeiro (Fase 0), esqueleto de backend/frontend/infraestrutura local em
seguida (Fase 1), e os demais diretórios à medida que cada domínio funcional é
implementado. Isso evita diretórios vazios ou com arquivos incompletos no repositório
antes que a fase correspondente exista de fato.

## 3. Alterações Futuras

Qualquer alteração relevante nesta estrutura (renomear diretório, mover módulo,
introduzir novo diretório de topo) deve ser registrada como um novo ADR, conforme
regra geral do projeto.

## Documentos Relacionados

- [Arquitetura](overview.md)
- [Roadmap](../roadmap.md)
