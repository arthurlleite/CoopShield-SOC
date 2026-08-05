import { useState } from 'react';
import { NavLink } from 'react-router-dom';
import { appRoutes } from '../routes';
import { ThemeToggle } from './ThemeToggle';

export function NavBar() {
  const [menuOpen, setMenuOpen] = useState(false);

  return (
    <header className="navbar">
      <div className="container navbar-inner">
        <NavLink to="/" className="navbar-brand" onClick={() => setMenuOpen(false)}>
          CoopShield SOC
        </NavLink>

        <button
          type="button"
          className="button navbar-toggle"
          aria-expanded={menuOpen}
          aria-controls="primary-navigation"
          onClick={() => setMenuOpen((open) => !open)}
        >
          Menu
        </button>

        <nav
          id="primary-navigation"
          className={menuOpen ? 'navbar-links navbar-links-open' : 'navbar-links'}
          aria-label="Navegação principal"
        >
          {appRoutes
            .filter((route) => route.showInNav)
            .map((route) => (
              <NavLink
                key={route.path}
                to={route.path}
                className={({ isActive }) => (isActive ? 'navbar-link navbar-link-active' : 'navbar-link')}
                onClick={() => setMenuOpen(false)}
              >
                {route.label}
              </NavLink>
            ))}
        </nav>

        <div className="navbar-actions">
          <ThemeToggle />
        </div>
      </div>
    </header>
  );
}
