import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

// Dados de exemplo — trocar pela resposta real de GET /api/desafios assim que
// a API estiver de pé. Estrutura pensada pra bater com o schema do plano técnico.
const DESAFIOS_EXEMPLO = [
  { id: 1, titulo: 'API de pedidos para uma pequena loja', tecnologias: 'Java, Spring Boot, MySQL', nivel: 'Intermediário' },
  { id: 2, titulo: 'Landing page com formulário de contato', tecnologias: 'React, CSS', nivel: 'Iniciante' },
  { id: 3, titulo: 'Dashboard de métricas em tempo real', tecnologias: 'React, WebSocket', nivel: 'Avançado' },
  { id: 4, titulo: 'CRUD de tarefas com autenticação', tecnologias: 'Spring Boot, JWT', nivel: 'Intermediário' },
  { id: 5, titulo: 'Consumo de API pública com cache', tecnologias: 'Java, Redis', nivel: 'Avançado' },
  { id: 6, titulo: 'Formulário multi-etapas responsivo', tecnologias: 'React, CSS Grid', nivel: 'Iniciante' },
];

// Nota de estilo: essa tela usa classes (.search-box, .challenge-grid, .challenge-card,
// .tag, .empty-state) que ainda não estão no theme.css compartilhado — copia o CSS
// delas do telas.html original pra um arquivo Desafios.css, ou direto aqui num <style>.
export default function Desafios() {
  const navigate = useNavigate();
  const [busca, setBusca] = useState('');

  const visiveis = DESAFIOS_EXEMPLO.filter((d) =>
    `${d.titulo} ${d.tecnologias} ${d.nivel}`.toLowerCase().includes(busca.toLowerCase().trim())
  );

  return (
    <div className="surface-paper">
      <header className="topbar">
        <div className="crumb">
          TRAMPOLIM / <b>DESAFIOS</b>
        </div>
      </header>

      <label className="search-box">
        <span>⌕</span>
        <input
          value={busca}
          onChange={(e) => setBusca(e.target.value)}
          type="search"
          placeholder="Busque por título, tecnologia ou empresa"
        />
      </label>

      <div className="result-heading">
        <h2>Para você explorar</h2>
        <span>
          {String(visiveis.length).padStart(2, '0')} {visiveis.length === 1 ? 'resultado' : 'resultados'}
        </span>
      </div>

      <div className="challenge-grid">
        {visiveis.map((d) => (
          <article key={d.id} className="challenge-card" onClick={() => navigate(`/desafios/${d.id}`)}>
            <div>{d.titulo}</div>
            <div className="tag amber">{d.tecnologias}</div>
            <div className="level">{d.nivel}</div>
          </article>
        ))}
      </div>

      {visiveis.length === 0 && (
        <div className="empty-state">Nenhum desafio com esses filtros. Tente remover uma tecnologia ou nível.</div>
      )}
    </div>
  );
}
