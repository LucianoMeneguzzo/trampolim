// IMPORTAÇÕES
import { useNavigate } from 'react-router';
import './PerfilEmpresa.css';

export default function PerfilEmpresa() {
  const navigate = useNavigate();

  const empresa = {
    nome: 'Nexa Tecnologia',
    descricao:
      'Desenvolvemos soluções digitais e aproximamos talentos de desafios reais do mercado, incentivando o aprendizado prático, a criatividade e a colaboração.',
    site: 'https://www.google.com',
    logo: 'NT',
  };

  const desafios = [
    {
      id: 1,
      titulo: 'API de pedidos',
      descricao:
        'Desenvolva uma API REST para gerenciamento de pedidos, com validações, tratamento de erros e documentação.',
      tecnologias: ['Java', 'Spring Boot', 'API REST'],
      participantes: 32,
      entregas: 18,
      status: 'Em andamento',
    },
    {
      id: 2,
      titulo: 'Dashboard de indicadores',
      descricao:
        'Crie um painel interativo para visualização de indicadores e análise de dados de negócio.',
      tecnologias: ['React', 'TypeScript', 'Chart.js'],
      participantes: 24,
      entregas: 15,
      status: 'Em andamento',
    },
    {
      id: 3,
      titulo: 'Landing page institucional',
      descricao:
        'Desenvolva uma página responsiva para apresentar os serviços e diferenciais de uma empresa.',
      tecnologias: ['HTML', 'CSS', 'JavaScript'],
      participantes: 40,
      entregas: 40,
      status: 'Encerrado',
    },
    {
      id: 4,
      titulo: 'Sistema de cadastro',
      descricao:
        'Implemente uma interface para cadastro e consulta de informações, com validação de formulários.',
      tecnologias: ['React', 'TypeScript'],
      participantes: 28,
      entregas: 0,
      status: 'Aberto',
    },
  ];

  const totalParticipantes = desafios.reduce(
    (total, desafio) => total + desafio.participantes,
    0,
  );

  const totalEntregas = desafios.reduce(
    (total, desafio) => total + desafio.entregas,
    0,
  );

  return (
    <main className="surface-paper">
      <header className="topbar">
        <div className="crumb">
          TRAMPOLIM / <b>PERFIL DA EMPRESA</b>
        </div>

        <div className="top-actions">
          <span className="status grey-status">
            VISÍVEL SEM LOGIN
          </span>

          <button
            type="button"
            className="text-button"
            onClick={() => navigate('/desafios')}
          >
            Explorar desafios
          </button>
        </div>
      </header>

      <section className="company-banner">
        <div className="company-logo" aria-label={`Logo ${empresa.nome}`}>
          {empresa.logo}
        </div>

        <div className="company-info">
          <h1>{empresa.nome}</h1>
          <div className="company-eyebrow">
            EMPRESA PARCEIRA · TRAMPOLIM
          </div>
          <p>{empresa.descricao}</p>
          <a
            href={empresa.site}
            target="_blank"
            rel="noreferrer"
            className="company-site"
          >
            Visitar site oficial ↗
          </a>
        </div>

        <div className="company-banner-actions">
          <button
            type="button"
            className="btn btn-outline-light"
            onClick={() => navigator.clipboard?.writeText(window.location.href)}
          >
            Compartilhar perfil ↗
          </button>
        </div>
      </section>

      <div className="company-content">
        <section>
          <div className="company-section-heading">
            <div>
              <h2>Desafios publicados</h2>
              <p>
                Conheça os projetos propostos pela {empresa.nome}.
              </p>
            </div>
            <button
              type="button"
              className="text-button"
              onClick={() => navigate('/desafios')}
            >
              Ver todos os desafios
            </button>
          </div>

          <div className="company-challenge-list">
            {desafios.map((desafio) => {
              const taxaEntrega =
                desafio.participantes > 0
                  ? Math.round(
                      (desafio.entregas / desafio.participantes) * 100,
                    )
                  : 0;

              const statusClass = {
                'Em andamento': 'status-progress',
                Aberto: 'status-open',
                Encerrado: 'status-closed',
              }[desafio.status];

              return (
                <article
                  className="company-challenge-card"
                  key={desafio.id}
                >
                  <div className="challenge-card-top">
                    <span className="challenge-category">
                      DESAFIO #{String(desafio.id).padStart(2, '0')}
                    </span>

                    <span className={`challenge-status ${statusClass}`}>
                      {desafio.status}
                    </span>
                  </div>

                  <h3>{desafio.titulo}</h3>

                  <p>
                    {desafio.descricao}
                  </p>

                  <div className="company-tech-list">
                    {desafio.tecnologias.map((tecnologia) => (
                      <span className="company-tech-tag" key={tecnologia}>
                        {tecnologia}
                      </span>
                    ))}
                  </div>

                  <div className="challenge-stats">
                    <div className="challenge-stat">
                      <span className="stat-symbol">♙</span>
                      <div>
                        <strong>{desafio.participantes}</strong>
                        <span> aceitaram</span>
                      </div>
                    </div>

                    <div className="challenge-stat">
                      <span className="stat-symbol stat-symbol-green">
                        ✓
                      </span>
                      <div>
                        <strong>{desafio.entregas}</strong>
                        <span> entregaram</span>
                      </div>
                    </div>

                    <div className="challenge-rate">
                      {taxaEntrega}% de entrega
                    </div>
                  </div>

                  <div
                    className="challenge-progress-track"
                    role="progressbar"
                    aria-label={`Taxa de entrega de ${desafio.titulo}`}
                    aria-valuenow={taxaEntrega}
                    aria-valuemin={0}
                    aria-valuemax={100}
                  >
                    <div
                      className="challenge-progress-fill"
                      style={{ width: `${taxaEntrega}%` }}
                    />
                  </div>

                  <div className="challenge-card-footer">
                    <span>
                      {desafio.entregas} de {desafio.participantes} entregas
                    </span>

                    <button
                      type="button"
                      className="challenge-link"
                      onClick={() => navigate('/desafios')}
                    >
                      Ver desafio
                    </button>
                  </div>
                </article>
              );
            })}
          </div>
        </section>

        <section className="company-metrics" aria-label="Indicadores da empresa">
          <article className="company-metric">
            <div className="metric-icon">
              <span aria-hidden="true">▤</span>
            </div>
            <div>
              <strong>{String(desafios.length).padStart(2, '0')}</strong>
              <span>DESAFIOS PUBLICADOS</span>
            </div>
          </article>

          <article className="company-metric">
            <div className="metric-icon">
              <span aria-hidden="true">♙</span>
            </div>
            <div>
              <strong>{totalParticipantes}</strong>
              <span>PARTICIPAÇÕES</span>
            </div>
          </article>

          <article className="company-metric">
            <div className="metric-icon metric-icon-green">
              <span aria-hidden="true">✓</span>
            </div>
            <div>
              <strong>{totalEntregas}</strong>
              <span>ENTREGAS REALIZADAS</span>
            </div>
          </article>
        </section>
      </div>
    </main>
  );
}
