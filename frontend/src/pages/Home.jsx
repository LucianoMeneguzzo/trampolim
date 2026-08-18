import { useNavigate } from 'react-router-dom';

// Reinterpretação do "commit graph" do GitHub, contando desafios concluídos
// em vez de commits diários. Gerado por padrão determinístico (mesmo espírito
// visual do protótipo original, sem precisar hardcodar cada quadradinho).
function ActivityGrid() {
  const total = 66;
  const dots = Array.from({ length: total }, (_, i) => {
    const seed = (i * 37) % 100;
    if (seed > 88) return 's3';
    if (seed > 76) return 's2';
    if (seed > 64) return 's1';
    return '';
  });
  return (
    <div className="activity-grid" aria-label="34 desafios concluídos">
      {dots.map((cls, i) => (
        <i key={i} className={cls || undefined} />
      ))}
    </div>
  );
}

export default function Home() {
  const navigate = useNavigate();

  return (
    <div className="home-shell">
      <nav className="home-nav">
        <span className="brand">
          <span className="brand-mark">T</span> Trampolim
        </span>
        <div className="home-nav-links">
          <button type="button" onClick={() => navigate('/desafios')}>Desafios</button>
          <button type="button" onClick={() => navigate('/perfil/1')}>Portfólios</button>
          <button type="button" onClick={() => navigate('/login')}>Entrar</button>
          <button type="button" className="btn btn-primary btn-small" onClick={() => navigate('/login')}>
            Criar perfil
          </button>
        </div>
      </nav>

      <div className="home-hero">
        <div className="home-copy">
          <div className="micro-label">Desafios reais · Portfólio validado</div>
          <h1>
            Seu currículo diz que você sabe.
            <br />
            <span>O Trampolim prova.</span>
          </h1>
          <p>
            Resolva desafios de tecnologia propostos por quem já atua na área. Cada entrega vira
            feedback, nota e uma prova pública do seu trabalho.
          </p>
          <div className="home-ctas">
            <button type="button" className="btn btn-primary" onClick={() => navigate('/desafios')}>
              Explorar desafios <span className="arrow">→</span>
            </button>
            <button type="button" className="btn btn-outline" onClick={() => navigate('/desafios/novo')}>
              Sou mentor
            </button>
          </div>
          <div className="trustline">
            <span>Sem promessas vazias</span>
            <i className="line" />
            <span>Repositórios e avaliações de verdade</span>
          </div>
        </div>

        <div className="home-proof">
          <article className="proof-card" aria-label="Exemplo de atividade de uma aluna">
            <div className="proof-top">
              <div>
                <div className="proof-kicker">perfil público · Ana Martins</div>
                <div className="proof-title">Trabalho comprovado</div>
              </div>
              <div className="mini-avatar">AM</div>
            </div>
            <ActivityGrid />
            <div className="activity-legend">
              <span>34 desafios concluídos</span>
              <span className="activity-key">
                menos <b /><b /><b /><b /> mais
              </span>
            </div>
          </article>
        </div>
      </div>

      <div className="home-strip">
        <div className="intro">
          De desafio prático
          <br />
          para uma carreira visível.
        </div>
        <div>
          <span className="step-no">01</span>
          <strong className="step-title">Escolha um desafio</strong>
          <span className="step-copy">Filtre por tecnologia e nível.</span>
        </div>
        <div>
          <span className="step-no">02</span>
          <strong className="step-title">Envie seu projeto</strong>
          <span className="step-copy">Validamos seu repositório no GitHub.</span>
        </div>
        <div>
          <span className="step-no">03</span>
          <strong className="step-title">Construa reputação</strong>
          <span className="step-copy">Receba feedback que vira portfólio.</span>
        </div>
      </div>
    </div>
  );
}
