// IMPORTAÇÕES
import { useNavigate } from 'react-router';
import './PerfilAluno.css'; 

export default function PerfilAluno() {
  const navigate = useNavigate();

  const trabalhosValidados = [
    {
      id: 1,
      titulo: 'API de pedidos',
      mentor: 'Rafael Freitas',
      feedback: 'Estrutura da API clara e boas respostas de erro.',
      nota: '8.5',
      tags: ['Java', 'Spring Boot', 'Avaliado'],
    },
    {
      id: 2,
      titulo: 'Dashboard de pedidos',
      mentor: 'Marina Salles',
      feedback: 'Interface bem organizada e fluxo de filtros consistente.',
      nota: '9.0',
      tags: ['React', 'API REST', 'Avaliado'],
    },
    {
      id: 3,
      titulo: 'Catálogo de filmes',
      mentor: 'Caio Mendes',
      feedback: 'Boa integração com API e atenção aos estados vazios.',
      nota: '8.0',
      tags: ['React', 'API externa', 'Avaliado'],
    },
  ];

  return (
    <div className="surface-paper">
      <header className="topbar">
        <div className="crumb">
          TRAMPOLIM / <b>PERFIL PÚBLICO</b>
        </div>

        <div className="top-actions">
          <span className="status grey-status">VISÍVEL SEM LOGIN</span>
          <button
            type="button"
            className="text-button"
            onClick={() => navigate('/desafios')}
          >
            Explorar desafios
          </button>
        </div>
      </header>

      <section className="profile-banner">
        <div className="avatar-large">AM</div>
        <div className="profile-info">
          <h1>Ana Martins</h1>
          <div className="label-orange">
            DESENVOLVEDORA FULL STACK EM FORMAÇÃO
          </div>
          <p>
            Transformo requisitos confusos em produtos simples de usar. Estou
            construindo experiência com React, Java e APIs.
          </p>

          <div className="profile-links">
            <a
              href="https://github.com/ana-martins"
              target="_blank"
              rel="noreferrer"
              className="link-badge"
            >
              github.com/ana-martins ↗
            </a>
            <a
              href="https://linkedin.com/in/ana-martins"
              target="_blank"
              rel="noreferrer"
              className="link-badge"
            >
              linkedin.com/in/ana-martins ↗
            </a>
          </div>
        </div>

        <div className="profile-banner-actions">
          <button
            type="button"
            className="btn btn-outline-light"
            onClick={() => navigator.clipboard?.writeText(window.location.href)}
          >
            Compartilhar perfil ↗
          </button>
        </div>
      </section>

      <div className="profile-layout">
        <section>
          <div className="section-title-wrap">
            <h2>Trabalho validado</h2>
            <button
              type="button"
              className="text-button"
              onClick={() => navigate('/desafios')}
            >
              Ver todos os desafios
            </button>
          </div>

          <div className="validated-list">
            {trabalhosValidados.map((item) => (
              <article className="validated-card" key={item.id}>
                <div>
                  <h3>{item.titulo}</h3>
                  <p>
                    Projeto avaliado por {item.mentor} ·{' '}
                    {item.feedback}
                  </p>

                  <div className="chip-group">
                    {item.tags.map((tag) => (
                      <span
                        key={tag}
                        className={`tag tag-chip-sm ${
                          tag === 'Avaliado' ? 'tag-chip-evaluated' : ''
                        }`}
                      >
                        {tag}
                      </span>
                    ))}
                  </div>
                </div>

                <div className="score-badge">{item.nota}</div>
              </article>
            ))}
          </div>
        </section>

        <aside className="profile-sidebar">
          <div className="metrics-grid">
            <div className="metric-box">
              <b>03</b>
              <span>DESAFIOS</span>
            </div>
            <div className="metric-box">
              <b>8.5</b>
              <span>MÉDIA</span>
            </div>
            <div className="metric-box">
              <b>07</b>
              <span>TECNOLOGIAS</span>
            </div>
          </div>

          <div className="activity-card">
            <h3>Ritmo de entregas</h3>
            <div className="activity-grid">
              {Array.from({ length: 30 }).map((_, index) => {
                let level = 'level-0';
                if ([3, 7, 12, 18].includes(index)) level = 'level-1';
                if ([8, 21].includes(index)) level = 'level-2';
                if ([1, 5, 10, 15].includes(index)) level = 'level-3';
                return <div key={index} className={`activity-square ${level}`} />;
              })}
            </div>
            <div className="activity-footer">
              <span>Últimos 6 meses</span>
              <span>entregas concluídas</span>
            </div>
          </div>

          <div className="quote-card">
            <p>
              “A estrutura da API está clara e demonstra atenção à experiência de quem vai consumir o serviço.”
            </p>
            <span>RAFAEL FREITAS · MENTOR</span>
          </div>
        </aside>
      </div>
    </div>
  );
}
