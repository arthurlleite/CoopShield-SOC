import type { JSX } from 'react';
import { AboutPage } from './pages/AboutPage';
import { AlertCenterPage } from './pages/AlertCenterPage';
import { ArchitecturePage } from './pages/ArchitecturePage';
import { AuditExplorerPage } from './pages/AuditExplorerPage';
import { DataProtectionCenterPage } from './pages/DataProtectionCenterPage';
import { DetectionRulesPage } from './pages/DetectionRulesPage';
import { DocumentationPage } from './pages/DocumentationPage';
import { IncidentWorkspacePage } from './pages/IncidentWorkspacePage';
import { LaboratoryPage } from './pages/LaboratoryPage';
import { LandingPage } from './pages/LandingPage';
import { LoginPage } from './pages/LoginPage';
import { SocDashboardPage } from './pages/SocDashboardPage';

export interface AppRoute {
  path: string;
  label: string;
  element: JSX.Element;
  showInNav: boolean;
}

export const appRoutes: AppRoute[] = [
  { path: '/', label: 'Início', element: <LandingPage />, showInNav: true },
  { path: '/dashboard', label: 'Dashboard', element: <SocDashboardPage />, showInNav: true },
  { path: '/alerts', label: 'Alertas', element: <AlertCenterPage />, showInNav: true },
  { path: '/incidents', label: 'Incidentes', element: <IncidentWorkspacePage />, showInNav: true },
  { path: '/detection-rules', label: 'Regras de Detecção', element: <DetectionRulesPage />, showInNav: true },
  { path: '/data-protection', label: 'Proteção de Dados', element: <DataProtectionCenterPage />, showInNav: true },
  { path: '/audit', label: 'Auditoria', element: <AuditExplorerPage />, showInNav: true },
  { path: '/laboratory', label: 'Laboratório', element: <LaboratoryPage />, showInNav: true },
  { path: '/architecture', label: 'Arquitetura', element: <ArchitecturePage />, showInNav: true },
  { path: '/documentation', label: 'Documentação', element: <DocumentationPage />, showInNav: true },
  { path: '/about', label: 'Sobre', element: <AboutPage />, showInNav: true },
  { path: '/login', label: 'Entrar', element: <LoginPage />, showInNav: true },
];
