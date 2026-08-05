interface PhaseNoticeProps {
  phase: string;
  description: string;
}

export function PhaseNotice({ phase, description }: PhaseNoticeProps) {
  return (
    <div className="card" style={{ borderStyle: 'dashed' }}>
      <span className="badge">Em construção</span>
      <p style={{ marginTop: '0.75rem' }}>
        {description} Funcionalidade completa disponível a partir da{' '}
        <strong>{phase}</strong>, conforme o{' '}
        <a
          href="https://github.com/arthurlleite/CoopShield-SOC/blob/main/docs/roadmap.md"
          target="_blank"
          rel="noreferrer"
        >
          roadmap do projeto
        </a>
        .
      </p>
    </div>
  );
}
