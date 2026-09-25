// Responsável: Luiz

import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router';
import './Desafio.css';

const DESAFIOS_MOCK = [
  { id: 1, mentor_id: 10, empresa_nome: 'Tech Corp', titulo: 'API de pedidos para uma loja', nivel: 'intermediario', status: 'aberto', tecnologias: ['Java', 'Spring Boot', 'MySQL'] },
  { id: 2, mentor_id: 11, empresa_nome: 'Design Co', titulo: 'Landing page com formulário de contato', nivel: 'iniciante', status: 'aberto', tecnologias: ['React', 'CSS'] },
  { id: 3, mentor_id: 12, empresa_nome: 'DataFlow', titulo: 'Dashboard de métricas em tempo real', nivel: 'avancado', status: 'aberto', tecnologias: ['React', 'WebSocket'] },
  { id: 4, mentor_id: 10, empresa_nome: 'TaskHub', titulo: 'CRUD de tarefas com autenticação', nivel: 'intermediario', status: 'aberto', tecnologias: ['Spring Boot', 'JWT'] },
  { id: 5, mentor_id: 13, empresa_nome: 'CacheOn', titulo: 'Consumo de API pública com cache', nivel: 'avancado', status: 'aberto', tecnologias: ['Java', 'Redis'] },
  { id: 6, mentor_id: 11, empresa_nome: 'FormIt', titulo: 'Formulário multi-etapas responsivo', nivel: 'iniciante', status: 'aberto', tecnologias: ['React', 'CSS Grid'] },
];

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

  useEffect(() => {
    const timer = setTimeout(() => {
      setDesafios(DESAFIOS_MOCK);
      setLoading(false);
    }, 300);

    return () => clearTimeout(timer);
  }, []);

  const visiveis = desafios.filter((d) => {
    const tecs = Array.isArray(d.tecnologias)
      ? d.tecnologias
      : (d.tecnologias || '').split(',').map((t) => t.trim());

    const matchBusca = `${d.titulo} ${tecs.join(' ')} ${d.nivel} ${d.empresa_nome || ''}`
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
          <p>Desafios reais propostos por mentores. Filtre por tecnologia, nível ou empresa.</p>
        </div>
      </div>

      <div className="challenge-toolbar">
        <label className="search-box">
          <span>⌕</span>
          <input
            value={busca}
            onChange={(e) => setBusca(e.target.value)}
            type="search"
            placeholder="Busque por título, tecnologia ou empresa"
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

            {d.empresa_nome && (
              <div className="card-empresa">
                <span className="empresa-logo">
                  {d.empresa_nome.charAt(0).toUpperCase()}
                </span>
                <span className="empresa-nome">{d.empresa_nome}</span>
              </div>
            )}

            <h3>{d.titulo}</h3>

            <div className="card-tags">
              {(Array.isArray(d.tecnologias)
                ? d.tecnologias
                : (d.tecnologias || '').split(',').map((t) => t.trim())
              ).map((tech, i) => (
                <span key={i} className="tag amber">{tech}</span>
              ))}
            </div>

            <div className="card-meta">
              <span>📊 {LABEL_NIVEL[d.nivel] || d.nivel}</span>
              {d.empresa_nome && <span>🏢 {d.empresa_nome}</span>}
            </div>

            <button className="card-cta">Ver desafio →</button>
          </article>
        ))}
      </div>

      {visiveis.length === 0 && (
        <div className="empty-state">
          Nenhum desafio com esses filtros. Tente remover uma tecnologia ou nível.
        </div>
      )}
    </div>
  );
}