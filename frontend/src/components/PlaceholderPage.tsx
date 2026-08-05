import type { ReactNode } from 'react';
import { PhaseNotice } from './PhaseNotice';

interface PlaceholderPageProps {
  title: string;
  description: string;
  phase: string;
  children?: ReactNode;
}

export function PlaceholderPage({ title, description, phase, children }: PlaceholderPageProps) {
  return (
    <div className="container" style={{ padding: '2.5rem 1.25rem' }}>
      <h1>{title}</h1>
      <p style={{ color: 'var(--color-text-muted)', maxWidth: 720 }}>{description}</p>
      {children}
      <PhaseNotice phase={phase} description="Esta página exibirá dados reais processados pelo backend." />
    </div>
  );
}
