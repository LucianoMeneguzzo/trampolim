// Responsável: Luiz

import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router';
import './Desafio.css';

const FILTROS_NIVEL = ['Todos', 'iniciante', 'intermediario', 'avancado'];

const LABEL_NIVEL = {
  iniciante: 'Iniciante',
  intermediario: 'Intermediário',
  avancado: 'Avançado',
};

export default function Desafio() {
  const navigate = useNavigate();

  const [busca, setBusca] = useState('');
  const [nivelAtivo, setNivelAtivo] = useState('Todos');
  const [desafios, setDesafios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState(null);

  // Backend: http://localhost:8080/api/desafios
  useEffect(() => {
    fetch('http://localhost:8080/api/desafios')
      .then((r) => {
        if (!r.ok) throw new Error(`HTTP ${r.status}`);
        return r.json();
      })
      .then((dados) => setDesafios(dados))
      .catch((e) => {
        console.error('Erro ao buscar desafios:', e);
        setErro('Não foi possível carregar os desafios. Tente novamente mais tarde.');
      })
      .finally(() => setLoading(false));
  }, []);

  const visiveis = desafios.filter((d) => {
    const matchBusca = `${d.titulo} ${d.descricao || ''} ${d.nivel} ${d.nomeCriador || ''}`
      .toLowerCase()
      .includes(busca.toLowerCase().trim());

    const matchNivel = nivelAtivo === 'Todos' || d.nivel === nivelAtivo;

    return matchBusca && matchNivel;
  });

  if (loading) {
    return (
      <div className="surface-paper">
        <header className="topbar">
          <div className="crumb">
            TRAMPOLIM / <b>DESAFIOS</b>
          </div>
        </header>
        <div className="desafios-loading">Carregando desafios...</div>
      </div>
    );
  }

  if (erro) {
    return (
      <div className="surface-paper">
        <header className="topbar">
          <div className="crumb">
            TRAMPOLIM / <b>DESAFIOS</b>
          </div>
        </header>
        <div className="empty-state">{erro}</div>
      </div>
    );
  }

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