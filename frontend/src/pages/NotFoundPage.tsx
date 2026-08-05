import { Link } from 'react-router-dom';

export function NotFoundPage() {
  return (
    <div className="container" style={{ padding: '3rem 1.25rem', textAlign: 'center' }}>
      <h1>Página não encontrada</h1>
      <p style={{ color: 'var(--color-text-muted)' }}>
        O endereço acessado não corresponde a nenhuma página do CoopShield SOC.
      </p>
      <Link className="button button-primary" to="/">
        Voltar à página inicial
      </Link>
    </div>
  );
}
