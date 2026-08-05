import { Link } from 'react-router-dom';

const TECH_GROUPS: Array<{ title: string; items: string[] }> = [
  {
    title: 'Backend',
    items: [
      'Java 21',
      'Spring Boot',
      'Spring Security',
      'Spring Data MongoDB',
      'Spring for Apache Kafka',
      'Spring Boot Actuator',
      'Maven',
      'OpenAPI / Swagger',
    ],
  },
  {
    title: 'Frontend',
    items: ['React', 'TypeScript', 'Vite', 'React Router'],
  },
  {
    title: 'Dados e Eventos',
    items: ['MongoDB', 'Apache Kafka', 'Regras de detecção em YAML'],
  },
  {
    title: 'Infraestrutura',
    items: ['Docker', 'Docker Compose', 'Kubernetes educacional', 'Prometheus', 'Grafana', 'OpenTelemetry'],
  },
];

const SCENARIOS = [
  'Conta possivelmente comprometida (falhas seguidas de sucesso)',
  'Abuso de privilégio administrativo',
  'Consulta massiva de clientes sintéticos',
  'Exportação atípica de dados',
  'Exposição de dado sensível em texto puro',
  'Autenticação em dispositivo desconhecido',
];

const DIFFERENTIALS = [
  'Segurança centrada no dado: tokenização e mascaramento antes de qualquer persistência ou publicação.',
  'Motor de detecção explicável, com regras versionadas em YAML e mapeamento MITRE ATT&CK.',
  'Motor de risco determinístico: cada pontuação mostra exatamente quais fatores contribuíram.',
  'Arquitetura hexagonal e monólito modular orientado a eventos, documentado via ADRs.',
  'Modo de demonstração estático (GitHub Pages) e modo completo (Docker Compose) a partir da mesma base de UI.',
];

export function LandingPage() {
  return (
    <div className="container" style={{ padding: '2.5rem 1.25rem' }}>
      <section style={{ textAlign: 'center', marginBottom: '3rem' }}>
        <span className="badge">Fase 1 em andamento — ver status abaixo</span>
        <h1 style={{ fontSize: '2.5rem', margin: '1rem 0 0.5rem' }}>CoopShield SOC</h1>
        <p style={{ fontSize: '1.15rem', color: 'var(--color-text-muted)', maxWidth: 720, margin: '0 auto' }}>
          Plataforma educacional de defesa cibernética e proteção de dados para um
          ambiente financeiro fictício — cooperativas, bancos e fintechs sintéticos.
        </p>
        <div style={{ display: 'flex', gap: '0.75rem', justifyContent: 'center', marginTop: '1.5rem', flexWrap: 'wrap' }}>
          <Link className="button button-primary" to="/laboratory">
            Explorar o laboratório de simulação
          </Link>
          <a
            className="button"
            href="https://github.com/arthurlleite/CoopShield-SOC"
            target="_blank"
            rel="noreferrer"
          >
            Ver no GitHub
          </a>
        </div>
      </section>

      <section className="card" style={{ marginBottom: '2rem' }}>
        <h2>O Problema</h2>
        <p>
          Instituições financeiras processam um volume enorme de eventos de
          autenticação, autorização, acesso a dados e administração. A maior parte é
          legítima; uma fração pequena representa risco real — contas comprometidas,
          abuso de privilégio, exportações anormais, exposição de dados sensíveis. O
          desafio de um SOC não é ter dados, é transformar volume em sinal acionável
          sem nunca expor dados sensíveis no processo.
        </p>
      </section>

      <section className="card" style={{ marginBottom: '2rem' }}>
        <h2>A Solução</h2>
        <p>
          O CoopShield SOC demonstra um pipeline completo: ingestão de eventos
          sintéticos, proteção de dados centrada no dado (tokenização e
          mascaramento), motor de detecção baseado em regras explicáveis mapeadas ao
          MITRE ATT&CK, motor de risco determinístico, gestão de alertas e
          incidentes, playbooks defensivos simulados e um laboratório de simulação
          interativo.
        </p>
      </section>

      <section style={{ marginBottom: '2rem' }}>
        <h2>Fluxo Defensivo</h2>
        <div className="card">
          <p style={{ fontFamily: 'var(--mono)', fontSize: '0.95rem', overflowX: 'auto' }}>
            evento sintético → normalização → tokenização → detecção (regras YAML) →
            cálculo de risco → alerta → incidente → investigação → playbook simulado
          </p>
        </div>
      </section>

      <section style={{ marginBottom: '2rem' }}>
        <h2>Principais Cenários do Laboratório</h2>
        <ul>
          {SCENARIOS.map((scenario) => (
            <li key={scenario}>{scenario}</li>
          ))}
        </ul>
      </section>

      <section style={{ marginBottom: '2rem' }}>
        <h2>Tecnologias</h2>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '1rem' }}>
          {TECH_GROUPS.map((group) => (
            <div className="card" key={group.title}>
              <h3 style={{ marginTop: 0 }}>{group.title}</h3>
              <ul style={{ margin: 0, paddingLeft: '1.1rem' }}>
                {group.items.map((item) => (
                  <li key={item}>{item}</li>
                ))}
              </ul>
            </div>
          ))}
        </div>
      </section>

      <section style={{ marginBottom: '2rem' }}>
        <h2>Diferenciais</h2>
        <ul>
          {DIFFERENTIALS.map((item) => (
            <li key={item}>{item}</li>
          ))}
        </ul>
      </section>

      <section style={{ marginBottom: '2rem' }}>
        <h2>Documentação</h2>
        <p>
          Arquitetura, modelo de ameaça STRIDE, catálogos de eventos e regras,
          decisões arquiteturais (ADRs) e o roadmap completo estão documentados no
          repositório. Veja a página{' '}
          <Link to="/documentation">Documentação</Link> para o índice completo.
        </p>
      </section>

      <section className="card">
        <h2>Roadmap e Status Atual</h2>
        <p>
          O projeto é construído em 15 fases sequenciais, cada uma validada e
          publicada antes do início da próxima. A Fase 0 (arquitetura e documentação)
          está concluída; a Fase 1 (estrutura de backend e frontend) está em
          andamento. Veja a página <Link to="/about">Sobre o Projeto</Link> para o
          status detalhado.
        </p>
      </section>
    </div>
  );
}
