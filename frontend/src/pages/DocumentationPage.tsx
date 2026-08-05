const BASE = 'https://github.com/arthurlleite/CoopShield-SOC/blob/main';

const SECTIONS: Array<{ title: string; links: Array<{ label: string; href: string }> }> = [
  {
    title: 'Produto',
    links: [
      { label: 'Visão do Produto', href: `${BASE}/docs/product/vision.md` },
      { label: 'Personas e Casos de Uso', href: `${BASE}/docs/product/personas-use-cases.md` },
    ],
  },
  {
    title: 'Segurança',
    links: [
      { label: 'Modelo de Ameaça (STRIDE)', href: `${BASE}/docs/threat-model/stride.md` },
      { label: 'Política de Segurança', href: `${BASE}/SECURITY.md` },
    ],
  },
  {
    title: 'Arquitetura',
    links: [
      { label: 'Visão Geral e Diagramas', href: `${BASE}/docs/architecture/overview.md` },
      { label: 'Perfis e Permissões', href: `${BASE}/docs/architecture/roles-permissions.md` },
      { label: 'Estrutura do Repositório', href: `${BASE}/docs/architecture/repository-structure.md` },
      { label: 'Riscos Técnicos', href: `${BASE}/docs/architecture/technical-risks.md` },
      { label: 'Decisões Arquiteturais (ADRs)', href: `${BASE}/docs/adr` },
    ],
  },
  {
    title: 'Catálogos',
    links: [
      { label: 'Catálogo de Eventos', href: `${BASE}/docs/event-catalog/events.md` },
      { label: 'Catálogo de Regras de Detecção', href: `${BASE}/docs/detection-rules/catalog.md` },
    ],
  },
  {
    title: 'Planejamento',
    links: [
      { label: 'Roadmap Completo', href: `${BASE}/docs/roadmap.md` },
      { label: 'Checklist de Conclusão da Fase 0', href: `${BASE}/docs/phase-0-completion-checklist.md` },
      { label: 'Planejamento da Fase 1', href: `${BASE}/docs/phase-1-plan.md` },
    ],
  },
  {
    title: 'Backend e Frontend',
    links: [
      { label: 'backend/README.md', href: `${BASE}/backend/README.md` },
      { label: 'frontend/README.md', href: `${BASE}/frontend/README.md` },
    ],
  },
];

export function DocumentationPage() {
  return (
    <div className="container" style={{ padding: '2.5rem 1.25rem' }}>
      <h1>Documentação</h1>
      <p style={{ color: 'var(--color-text-muted)', maxWidth: 720 }}>
        Índice da documentação completa do projeto, versionada junto ao código no
        repositório GitHub.
      </p>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1rem' }}>
        {SECTIONS.map((section) => (
          <div className="card" key={section.title}>
            <h2 style={{ marginTop: 0 }}>{section.title}</h2>
            <ul style={{ paddingLeft: '1.1rem', margin: 0 }}>
              {section.links.map((link) => (
                <li key={link.href}>
                  <a href={link.href} target="_blank" rel="noreferrer">
                    {link.label}
                  </a>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
    </div>
  );
}
