import { PlaceholderPage } from '../components/PlaceholderPage';

export function DetectionRulesPage() {
  return (
    <PlaceholderPage
      title="Detection Rules"
      description="Catálogo de regras de detecção carregadas de YAML, com severidade, limites, mapeamento MITRE ATT&CK, playbook recomendado e notas de falso positivo."
      phase="Fase 6 — Motor de Detecção"
    >
      <p>
        O catálogo conceitual das 15 regras iniciais já está documentado em{' '}
        <a
          href="https://github.com/arthurlleite/CoopShield-SOC/blob/main/docs/detection-rules/catalog.md"
          target="_blank"
          rel="noreferrer"
        >
          docs/detection-rules/catalog.md
        </a>
        .
      </p>
    </PlaceholderPage>
  );
}
