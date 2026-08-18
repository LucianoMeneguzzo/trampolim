import { NavLink, Outlet } from 'react-router-dom';

const PAGINAS = [
  { to: '/', num: '01', label: 'Home', end: true },
  { to: '/login', num: '02', label: 'Login e cadastro' },
  { to: '/desafios', num: '03', label: 'Lista de desafios' },
  { to: '/desafios/1', num: '04', label: 'Detalhe do desafio' },
  { to: '/desafios/1/enviar', num: '05', label: 'Envio do projeto' },
  { to: '/desafios/novo', num: '06', label: 'Criar desafio' },
  { to: '/avaliar/1', num: '07', label: 'Avaliar envio' },
  { to: '/perfil/1', num: '08', label: 'Perfil público' },
  { to: '/painel', num: '09', label: 'Painel de métricas' },
];

// Os links das telas 04, 05 e 07 usam um id de exemplo (1) só pra manter
// a navegação clicável durante o desenvolvimento — na tela real, quem leva
// pra lá é o clique num card de desafio específico, não esse menu.
export default function Layout() {
  return (
    <div className="app-shell">
      <aside className="rail">
        <NavLink to="/" className="brand">
          <span className="brand-mark">T</span> Trampolim
        </NavLink>
        <div className="eyebrow">Trampolim · 9 telas</div>
        <nav className="screen-nav" aria-label="Navegação principal">
          {PAGINAS.map((p) => (
            <NavLink
              key={p.to}
              to={p.to}
              end={p.end}
              className={({ isActive }) => (isActive ? 'is-active' : undefined)}
            >
              <span className="nav-no">{p.num}</span>
              {p.label}
            </NavLink>
          ))}
        </nav>
        <div className="rail-note">
          <strong>Desenho de fluxo</strong>
          Aluno → desafio → entrega → avaliação → portfólio validado.
        </div>
      </aside>
      <main className="page">
        <Outlet />
      </main>
    </div>
  );
}
