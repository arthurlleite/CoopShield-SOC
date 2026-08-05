const MODULES: Array<{ name: string; responsibility: string }> = [
  { name: 'identity', responsibility: 'Autenticação, usuários, refresh tokens, política de senha e bloqueio' },
  { name: 'accesscontrol', responsibility: 'Autorização por perfil (RBAC), verificação de permissões' },
  { name: 'eventingestion', responsibility: 'Recepção e validação inicial de eventos brutos (API/Kafka)' },
  { name: 'eventnormalization', responsibility: 'Normalização de eventos para o modelo comum' },
  { name: 'dataprotection', responsibility: 'Classificação, tokenização, mascaramento e destokenização' },
  { name: 'detection', responsibility: 'Carregamento de regras YAML e avaliação de eventos normalizados' },
  { name: 'risk', responsibility: 'Cálculo de pontuação de risco explicável' },
  { name: 'alert', responsibility: 'Ciclo de vida de alertas' },
  { name: 'incident', responsibility: 'Ciclo de vida de incidentes, evidências, linha do tempo' },
  { name: 'playbook', responsibility: 'Playbooks defensivos simulados e ações simuladas' },
  { name: 'audit', responsibility: 'Trilha de auditoria de ações sensíveis e privilegiadas' },
  { name: 'observability', responsibility: 'Métricas, health checks, correlação de logs' },
  { name: 'simulation', responsibility: 'Geração de personagens, cenários e eventos sintéticos' },
  { name: 'sharedkernel', responsibility: 'Tipos e contratos comuns entre módulos' },
];

export function ArchitecturePage() {
  return (
    <div className="container" style={{ padding: '2.5rem 1.25rem' }}>
      <h1>Arquitetura</h1>
      <p style={{ color: 'var(--color-text-muted)', maxWidth: 760 }}>
        O CoopShield SOC adota um monólito modular orientado a eventos, estruturado
        internamente com arquitetura hexagonal (portas e adaptadores) e princípios de
        Domain-Driven Design. Cada módulo mantém seu domínio isolado de detalhes de
        infraestrutura (REST, Kafka, MongoDB), permitindo, no futuro, extrair um
        módulo para um microsserviço independente sem reescrever sua lógica de
        negócio.
      </p>

      <section className="card" style={{ margin: '1.5rem 0' }}>
        <h2 style={{ marginTop: 0 }}>Fluxo Orientado a Eventos</h2>
        <p style={{ fontFamily: 'var(--mono)', fontSize: '0.9rem', overflowX: 'auto' }}>
          simulator → eventingestion → eventnormalization → dataprotection → detection
          → risk → alert → incident
        </p>
        <p>
          Cada evento carrega um <code>correlationId</code>, permitindo reconstruir a
          jornada completa entre ingestão, detecção, alerta e incidente. Dados
          sensíveis sintéticos são tokenizados pelo módulo <code>dataprotection</code>
          {' '}antes de qualquer persistência ou publicação para análise.
        </p>
      </section>

      <section style={{ marginBottom: '1.5rem' }}>
        <h2>Módulos do Backend</h2>
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr>
                <th style={{ textAlign: 'left', borderBottom: '1px solid var(--color-border)', padding: '0.5rem' }}>Módulo</th>
                <th style={{ textAlign: 'left', borderBottom: '1px solid var(--color-border)', padding: '0.5rem' }}>Responsabilidade</th>
              </tr>
            </thead>
            <tbody>
              {MODULES.map((module) => (
                <tr key={module.name}>
                  <td style={{ padding: '0.5rem', borderBottom: '1px solid var(--color-border)' }}>
                    <code>{module.name}</code>
                  </td>
                  <td style={{ padding: '0.5rem', borderBottom: '1px solid var(--color-border)' }}>
                    {module.responsibility}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      <section className="card">
        <h2 style={{ marginTop: 0 }}>Diagramas Completos</h2>
        <p>
          Os diagramas Mermaid de contexto, módulos, fluxo de eventos e modelo de
          dados conceitual estão em{' '}
          <a
            href="https://github.com/arthurlleite/CoopShield-SOC/blob/main/docs/architecture/overview.md"
            target="_blank"
            rel="noreferrer"
          >
            docs/architecture/overview.md
          </a>
          , junto com as decisões arquiteturais registradas nos{' '}
          <a
            href="https://github.com/arthurlleite/CoopShield-SOC/tree/main/docs/adr"
            target="_blank"
            rel="noreferrer"
          >
            ADRs
          </a>
          .
        </p>
      </section>
    </div>
  );
}
