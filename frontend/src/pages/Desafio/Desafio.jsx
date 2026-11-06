// Responsável: Luiz

import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import ContaTopbar from '../../components/ContaTopbar/ContaTopbar';
import { listarDesafios } from '../../services/desafios';
import { LABEL_NIVEL, listaDeTecnologias } from '../../utils/desafios';
import './Desafio.css';

const FILTROS_NIVEL = ['Todos', 'INICIANTE', 'INTERMEDIARIO', 'AVANCADO'];

export default function Desafio() {
  const navigate = useNavigate();

  const [busca, setBusca] = useState('');
  const [nivelAtivo, setNivelAtivo] = useState('Todos');
  const [desafios, setDesafios] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState('');

  useEffect(() => {
    let ativo = true;

    listarDesafios()
      .then((lista) => {
        // Desafio fechado não recebe inscrição, então não aparece para quem está procurando
        if (ativo) setDesafios(lista.filter((d) => d.status !== 'FECHADO'));
      })
      .catch(() => {
        if (ativo) setErro('Não consegui carregar os desafios. Confira se o back-end está rodando.');
      })
      .finally(() => {
        if (ativo) setCarregando(false);
      });

    return () => { ativo = false; };
  }, []);

  const visiveis = desafios.filter((d) => {
    const matchBusca = `${d.titulo} ${d.descricao || ''} ${d.tecnologias || ''} ${LABEL_NIVEL[d.nivel] || d.nivel} ${d.empresa || ''} ${d.nomeCriador || ''}`
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
          <button type="button" className="plain-link" onClick={() => navigate('/metricas')}>Métricas</button>
          <ContaTopbar />
        </div>
      </header>

      <div className="page-header">
        <div>
          <h1>Encontre seu próximo desafio</h1>
          <p>Desafios reais propostos por mentores. Filtre por nível ou busque por tecnologia.</p>
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
        {visiveis.map((d) => {
          const autor = d.empresa || d.nomeCriador;

          return (
            <article
              key={d.id}
              className="challenge-card"
              onClick={() => navigate(`/student/challenge/${d.id}`)}
            >
              <div className={`card-stripe nivel-${d.nivel.toLowerCase()}`} />

              {autor && (
                <div className="card-empresa">
                  <span className="empresa-logo">
                    {autor.charAt(0).toUpperCase()}
                  </span>
                  <span className="empresa-nome">{autor}</span>
                </div>
              )}

              <h3>{d.titulo}</h3>

              {d.descricao && (
                <p className="card-descricao">{d.descricao}</p>
              )}

              <div className="card-meta">
                <span>📊 {LABEL_NIVEL[d.nivel] || d.nivel}</span>
                {listaDeTecnologias(d.tecnologias).length > 0 && (
                  <span>🧰 {listaDeTecnologias(d.tecnologias).join(' · ')}</span>
                )}
              </div>

              <button type="button" className="card-cta">Ver desafio →</button>
            </article>
          );
        })}
      </div>

      {carregando && <div className="desafios-loading">Carregando desafios…</div>}

      {erro && <div className="empty-state" role="alert">{erro}</div>}

      {!carregando && !erro && visiveis.length === 0 && (
        <div className="empty-state">
          {desafios.length === 0
            ? 'Ainda não há desafios abertos. Volte em breve.'
            : 'Nenhum desafio com esses filtros. Tente remover algum filtro.'}
        </div>
      )}
    </div>
  );
}
