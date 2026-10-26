// Responsável: Luiz

import { useState } from 'react';
import { useNavigate } from 'react-router';
import './Desafio.css';

const FILTROS_NIVEL = ['Todos', 'iniciante', 'intermediario', 'avancado'];

const LABEL_NIVEL = {
  iniciante: 'Iniciante',
  intermediario: 'Intermediário',
  avancado: 'Avançado',
};

// --- DADOS FALSOS EXPANDIDOS (Para simular um banco de dados real) ---
const DESAFIOS_FALSOS = [
  { id: 1, titulo: 'Criar uma API REST', descricao: 'Desenvolva uma API usando Node.js e Express com autenticação JWT.', nivel: 'intermediario', nomeCriador: 'Tech Solutions' },
  { id: 2, titulo: 'Landing Page Responsiva', descricao: 'Crie uma página de vendas usando HTML, CSS e animações suaves.', nivel: 'iniciante', nomeCriador: 'Agência Web' },
  { id: 3, titulo: 'Sistema de Autenticação', descricao: 'Implemente login com JWT, rotas protegidas e React Context.', nivel: 'avancado', nomeCriador: 'Startup X' },
  { id: 4, titulo: 'Dashboard Analítico', descricao: 'Construa um painel de dados com gráficos interativos usando Chart.js.', nivel: 'intermediario', nomeCriador: 'DataCorp' },
  { id: 5, titulo: 'App de Clima', descricao: 'Consuma uma API externa para exibir a previsão do tempo em tempo real.', nivel: 'iniciante', nomeCriador: 'WebMasters' },
  { id: 6, titulo: 'E-commerce Completo', descricao: 'Desenvolva o frontend de uma loja virtual com carrinho e checkout.', nivel: 'avancado', nomeCriador: 'ShopDev' },
  { id: 7, titulo: 'Clone do Spotify', descricao: 'Recrie a interface de um player de música com controle de áudio.', nivel: 'intermediario', nomeCriador: 'MusicTech' },
  { id: 8, titulo: 'Calculadora Científica', descricao: 'Crie uma calculadora funcional com operações matemáticas complexas.', nivel: 'iniciante', nomeCriador: 'CodeLab' },
  { id: 9, titulo: 'Microserviço de Pagamentos', descricao: 'Integre um gateway de pagamento usando Node.js e Webhooks.', nivel: 'avancado', nomeCriador: 'FinTech BR' }
];
// ---------------------------------------------------------------------

export default function Desafio() {
  const navigate = useNavigate();

  const [busca, setBusca] = useState('');
  const [nivelAtivo, setNivelAtivo] = useState('Todos');
  
  const desafios = DESAFIOS_FALSOS; 

  const visiveis = desafios.filter((d) => {
    const matchBusca = `${d.titulo} ${d.descricao || ''} ${d.nivel} ${d.nomeCriador || ''}`
      .toLowerCase()
      .includes(busca.toLowerCase().trim());

    const matchNivel = nivelAtivo === 'Todos' || d.nivel === nivelAtivo;

    return matchBusca && matchNivel;
  });

  return (
    <div className="surface-paper">
      <header className="topbar">
        <div className="crumb">
          <button
            className="back-btn"
            onClick={() => navigate('/')}
            title="Voltar para a Home"
          >
            ←
          </button>
          TRAMPOLIM / <b>DESAFIOS</b>
        </div>
        <div className="top-actions">
          <div className="avatar">AM</div>
        </div>
      </header>

      <div className="page-header">
        <div>
          <h1>Encontre seu próximo desafio</h1>
          <p>Desafios reais propostos por mentores. Filtre por nível ou criador.</p>
        </div>
      </div>

      <div className="challenge-toolbar">
        <label className="search-box">
          <span>⌕</span>
          <input
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
            type="search"
            placeholder="Busque por título, descrição ou criador"
          />
        </label>
      </div>

      <div className="filter-row">
        <span className="filter-label">Nível</span>
        {FILTROS_NIVEL.map((nivel) => (
          <button
            key={nivel}
            type="button"
            className={`filter ${nivelAtivo === nivel ? 'is-selected' : ''}`}
            onClick={() => setNivelAtivo(nivel)}
          >
            {nivel === 'Todos' ? 'Todos' : LABEL_NIVEL[nivel]}
          </button>
        ))}
      </div>

      <div className="result-heading">
        <h2>Para você explorar</h2>
        <span>
          {String(visiveis.length).padStart(2, '0')}{' '}
          {visiveis.length === 1 ? 'resultado' : 'resultados'}
        </span>
      </div>

      <div className="challenge-grid">
        {visiveis.map((d) => (
          <article
            key={d.id}
            className="challenge-card"
            onClick={() => navigate(`/student/challenge/${d.id}`)}
          >
            <div className={`card-stripe nivel-${d.nivel}`} />

            {d.nomeCriador && (
              <div className="card-empresa">
                <span className="empresa-logo">
                  {d.nomeCriador.charAt(0).toUpperCase()}
                </span>
                <span className="empresa-nome">{d.nomeCriador}</span>
              </div>
            )}

            <h3>{d.titulo}</h3>

            {d.descricao && (
              <p className="card-descricao">{d.descricao}</p>
            )}

            <div className="card-meta">
              <span>📊 {LABEL_NIVEL[d.nivel] || d.nivel}</span>
              {d.nomeCriador && <span>🏢 {d.nomeCriador}</span>}
            </div>

            <button className="card-cta">Ver desafio →</button>
          </article>
        ))}
      </div>

      {visiveis.length === 0 && (
        <div className="empty-state">
          Nenhum desafio com esses filtros. Tente remover algum filtro.
        </div>
      )}
    </div>
  );
}