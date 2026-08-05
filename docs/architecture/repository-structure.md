# Estrutura do Repositório — CoopShield SOC

## 1. Estrutura Alvo

A estrutura abaixo é a estrutura alvo do repositório ao final das 15 fases (0 a 14).
Nesta Fase 0, apenas os diretórios de documentação e os arquivos de governança do
repositório existem; os demais diretórios (`backend/`, `frontend/`, `simulator/`,
`detection-rules/`, `infrastructure/`, `load-tests/`, `.github/workflows/`) serão
criados nas fases correspondentes, conforme o [Roadmap](../roadmap.md).

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
├── simulator/                   (Fase 5+)
│   ├── scenarios/
│   ├── synthetic-data/
│   └── README.md
├── detection-rules/             (Fase 6+)
│   ├── authentication/
│   ├── authorization/
│   ├── data-access/
│   ├── administration/
│   └── api/
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
