# CoopShield SOC — Frontend

> Projeto independente, educacional e de portfólio. Todos os dados, usuários, eventos,
> transações e incidentes apresentados são sintéticos.

Frontend React 19 / TypeScript / Vite do CoopShield SOC.

## Pré-requisitos

- Node.js 20.19+ ou 22.12+ (testado com Node 22.18.0). Versões anteriores do Node 18
  não são suportadas pelas ferramentas de build atuais (Vite 8, `create-vite`).

## Executar Localmente

```bash
cd frontend
npm install
npm run dev
```

A aplicação sobe em `http://localhost:5173`.

## Build

```bash
cd frontend
npm run build
```

Executa a checagem de tipos (`tsc -b`) e o build de produção do Vite, gerando os
arquivos estáticos em `dist/`.

## Estrutura

```
src/
├── components/     (Layout, NavBar, ThemeToggle, DisclaimerBanner, PlaceholderPage...)
├── pages/          (as 12 páginas do produto)
├── theme/          (ThemeProvider — modo claro/escuro com persistência em localStorage)
├── routes.tsx       (fonte única de verdade das rotas, usada pela navegação e pelo router)
├── App.tsx
└── main.tsx
```

## Páginas

| Rota | Página | Status nesta fase |
|------|--------|--------------------|
| `/` | Landing Page | Conteúdo completo (não depende de backend) |
| `/dashboard` | SOC Dashboard | Placeholder — dados reais na Fase 10 |
| `/alerts` | Alert Center | Placeholder — dados reais na Fase 8 |
| `/incidents` | Incident Workspace | Placeholder — dados reais na Fase 8 |
| `/detection-rules` | Detection Rules | Placeholder — dados reais na Fase 6 |
| `/data-protection` | Data Protection Center | Placeholder — dados reais na Fase 9 |
| `/audit` | Audit Explorer | Placeholder — dados reais na Fase 9 |
| `/laboratory` | Laboratory | Placeholder — funcional na Fase 5 |
| `/architecture` | Architecture | Conteúdo completo (não depende de backend) |
| `/documentation` | Documentation | Conteúdo completo (índice de links) |
| `/about` | About the Project | Conteúdo completo |
| `/login` | Login | Formulário funcional; autenticação real chega na Fase 2 |

Todas as páginas "placeholder" exibem claramente uma faixa "Em construção" indicando
a fase do roadmap em que a funcionalidade completa chega — nunca simulam dados que não
existem de fato.

## Tema Claro/Escuro

Implementado em `src/theme/ThemeProvider.tsx`: respeita `prefers-color-scheme` por
padrão e permite alternância manual via o botão no cabeçalho, persistida em
`localStorage`. Validado manualmente (Playwright headless) nos dois modos.

## Responsividade e Acessibilidade

- Navegação em grade flexível que colapsa em um menu vertical abaixo de 860px de
  largura, com botão de alternância acessível por teclado (`aria-expanded`,
  `aria-controls`).
- Foco visível (`:focus-visible`) em todos os elementos interativos.
- Formulário de login com `label` associado a cada campo e mensagem de status via
  `role="status"`/`aria-live="polite"`.

## Imagem Docker

```bash
docker build -t coopshield-soc-frontend -f frontend/Dockerfile frontend
docker run -p 8081:80 coopshield-soc-frontend
```

Build multi-stage: Node 22 para compilar os assets estáticos, Nginx Alpine para
servir o conteúdo em produção, com fallback de rota para `index.html` (necessário
para o React Router) e headers básicos de segurança.

## Dependências e Segurança

`react-router-dom` está na versão mais recente disponível (7.18.2). O `npm audit`
reporta um advisory de severidade alta (GHSA-qwww-vcr4-c8h2, CSRF em "RSC Mode") sem
correção publicada até esta fase; a aplicação é uma SPA client-side renderizada via
Vite e não utiliza React Server Components/RSC mode, portanto a superfície vulnerável
não é exercitada por este projeto. Risco aceito e documentado — reavaliar quando uma
versão corrigida for publicada.

## Limitações desta Fase

- Sem conexão real com o backend ainda (Live Mode chega quando os módulos de domínio
  tiverem endpoints, a partir da Fase 2).
- Sem modo de demonstração com dados JSON estáticos ainda (Fase 13).
- Sem testes automatizados de frontend ainda (Vitest/Playwright, Fase 12).
