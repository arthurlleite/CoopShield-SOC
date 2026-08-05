import { Outlet } from 'react-router-dom';
import { DisclaimerBanner } from './DisclaimerBanner';
import { NavBar } from './NavBar';

export function Layout() {
  return (
    <>
      <DisclaimerBanner />
      <NavBar />
      <main style={{ flex: 1 }}>
        <Outlet />
      </main>
      <footer className="app-footer">
        <p>
          CoopShield SOC — projeto independente e educacional. Código-fonte em{' '}
          <a href="https://github.com/arthurlleite/CoopShield-SOC" target="_blank" rel="noreferrer">
            github.com/arthurlleite/CoopShield-SOC
          </a>
          .
        </p>
      </footer>
    </>
  );
}
