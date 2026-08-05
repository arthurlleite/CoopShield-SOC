const PHASES: Array<{ name: string; status: 'Concluída' | 'Em andamento' | 'Não iniciada' }> = [
  { name: 'Fase 0 — Arquitetura e documentação inicial', status: 'Concluída' },
  { name: 'Fase 1 — Estrutura do back-end e front-end', status: 'Em andamento' },
  { name: 'Fase 2 — Autenticação e autorização', status: 'Não iniciada' },
  { name: 'Fase 3 — MongoDB e modelos de domínio', status: 'Não iniciada' },
  { name: 'Fase 4 — Kafka e ingestão de eventos', status: 'Não iniciada' },
  { name: 'Fase 5 — Simulador e dados sintéticos', status: 'Não iniciada' },
  { name: 'Fase 6 — Motor de detecção', status: 'Não iniciada' },
  { name: 'Fase 7 — Motor de risco', status: 'Não iniciada' },
  { name: 'Fase 8 — Alertas e incidentes', status: 'Não iniciada' },
  { name: 'Fase 9 — Proteção de dados', status: 'Não iniciada' },
  { name: 'Fase 10 — Dashboard e laboratório visual', status: 'Não iniciada' },
  { name: 'Fase 11 — Observabilidade', status: 'Não iniciada' },
  { name: 'Fase 12 — Testes e DevSecOps', status: 'Não iniciada' },
  { name: 'Fase 13 — GitHub Pages e modo demonstração', status: 'Não iniciada' },
  { name: 'Fase 14 — Documentação, validação e entrega final', status: 'Não iniciada' },
];

export function AboutPage() {
  return (
    <div className="container" style={{ padding: '2.5rem 1.25rem' }}>
      <h1>Sobre o Projeto</h1>

      <section className="card" style={{ marginBottom: '1.5rem' }}>
        <h2 style={{ marginTop: 0 }}>Independência e Dados Sintéticos</h2>
        <p>
          O CoopShield SOC é um projeto independente, educacional e de portfólio. Não
          possui vínculo com Sicoob, Itaú, Comforte, TAMUNIO, bancos, cooperativas,
          empresas de segurança ou instituições financeiras reais. Não utiliza
          logotipos, identidade visual proprietária, nomes de clientes ou
          funcionários reais, dados bancários reais ou qualquer informação
          confidencial. Todos os dados, usuários, eventos, transações e incidentes
          apresentados são sintéticos.
        </p>
      </section>

      <section style={{ marginBottom: '1.5rem' }}>
        <h2>Status das Fases</h2>
        <ul>
          {PHASES.map((phase) => (
            <li key={phase.name}>
              {phase.name} — <strong>{phase.status}</strong>
            </li>
          ))}
        </ul>
      </section>

      <section className="card">
        <h2 style={{ marginTop: 0 }}>Autoria e Licença</h2>
        <p>
          Desenvolvido por Arthur Carvalho Leite como projeto de portfólio técnico.
          Distribuído sob a{' '}
          <a
            href="https://github.com/arthurlleite/CoopShield-SOC/blob/main/LICENSE"
            target="_blank"
            rel="noreferrer"
          >
            licença MIT
          </a>
          . Código-fonte completo disponível em{' '}
          <a href="https://github.com/arthurlleite/CoopShield-SOC" target="_blank" rel="noreferrer">
            github.com/arthurlleite/CoopShield-SOC
          </a>
          .
        </p>
      </section>
    </div>
  );
}
