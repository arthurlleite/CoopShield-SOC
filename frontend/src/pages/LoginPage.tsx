import { useState, type FormEvent } from 'react';

export function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState<string | null>(null);

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setMessage(
      'Autenticação ainda não está conectada a um backend real. O módulo ' +
        'identity (JWT, refresh token, bloqueio temporário) chega na Fase 2 ' +
        'do roadmap.',
    );
  }

  return (
    <div className="container" style={{ padding: '2.5rem 1.25rem', maxWidth: 420 }}>
      <h1>Entrar</h1>
      <p style={{ color: 'var(--color-text-muted)' }}>
        Acesso ao modo local (Live Mode) do CoopShield SOC.
      </p>

      <form className="card" onSubmit={handleSubmit} noValidate>
        <div style={{ marginBottom: '1rem' }}>
          <label htmlFor="username" style={{ display: 'block', marginBottom: '0.25rem', fontWeight: 600 }}>
            Usuário sintético
          </label>
          <input
            id="username"
            name="username"
            type="text"
            required
            autoComplete="username"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            style={{
              width: '100%',
              padding: '0.5rem',
              borderRadius: 'var(--radius)',
              border: '1px solid var(--color-border)',
              background: 'var(--color-bg)',
              color: 'var(--color-text)',
            }}
          />
        </div>

        <div style={{ marginBottom: '1.25rem' }}>
          <label htmlFor="password" style={{ display: 'block', marginBottom: '0.25rem', fontWeight: 600 }}>
            Senha
          </label>
          <input
            id="password"
            name="password"
            type="password"
            required
            autoComplete="current-password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            style={{
              width: '100%',
              padding: '0.5rem',
              borderRadius: 'var(--radius)',
              border: '1px solid var(--color-border)',
              background: 'var(--color-bg)',
              color: 'var(--color-text)',
            }}
          />
        </div>

        <button type="submit" className="button button-primary" style={{ width: '100%' }}>
          Entrar
        </button>

        <p role="status" aria-live="polite" style={{ marginTop: '1rem', fontSize: '0.9rem' }}>
          {message}
        </p>
      </form>
    </div>
  );
}
