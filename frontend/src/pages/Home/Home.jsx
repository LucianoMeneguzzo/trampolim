import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router'
import Logo from '../../components/Logo/Logo'
import { getUser } from '../../services/api'
import { destinoInicial } from '../../services/auth'
import { buscarMetricas } from '../../services/usuarios'
import './Home.css'

const activityCells = [
  '', '', 's1', '', 's2', '', '', 's3', '', '', 's1', '',
  's2', '', '', 's1', '', 's3', '', '', 's2', '', '',
  '', 's1', '', 's2', '', '', 's1', 's3', '', '', 's2',
  '', '', 's1', '', 's3', '', 's2', '', '', 's1', '', '',
  's2', '', '', 's1', '', 's3', '', '', 's2', '', '',
  '', 's1', '', 's2', '', '', 's1', 's3', '', '', 's2',
]

export default function Home() {
  const navigate = useNavigate()
  const usuario = getUser()
  const [numeros, setNumeros] = useState(null)

  // Números reais da plataforma. Se a API não responder, o cartão fica só como ilustração.
  useEffect(() => {
    let ativo = true
    buscarMetricas()
      .then((metricas) => { if (ativo) setNumeros(metricas) })
      .catch(() => {})
    return () => { ativo = false }
  }, [])

  const resumo = numeros
    ? `${numeros.desafios} ${numeros.desafios === 1 ? 'desafio publicado' : 'desafios publicados'} · ${numeros.avaliados} ${numeros.avaliados === 1 ? 'entrega avaliada' : 'entregas avaliadas'}`
    : 'Exemplo de atividade'

  return (
    <main className="home-shell">
      <nav className="home-nav" aria-label="Navegação principal">
        <Logo />

        <div className="home-nav-links">
          <button type="button" onClick={() => navigate('/metricas')}>Métricas</button>
          {usuario ? (
            <button type="button" className="btn btn-primary btn-small" onClick={() => navigate(destinoInicial(usuario.type))}>
              Meu perfil
            </button>
          ) : (
            <>
              <button type="button" onClick={() => navigate('/login')}>Entrar</button>
              <button type="button" className="btn btn-primary btn-small" onClick={() => navigate('/register')}>
                Criar perfil
              </button>
            </>
          )}
        </div>
      </nav>

      <section className="home-hero" aria-labelledby="home-title">
        <div className="home-copy">
          <div className="micro-label">Desafios reais · Portfólio validado</div>
          <h1 id="home-title">
            Seu currículo diz que você sabe.<br />
            <span>O Trampolim prova.</span>
          </h1>
          <p>
            Resolva desafios de tecnologia propostos por quem já atua na área. Cada entrega vira
            feedback, nota e uma prova pública do seu trabalho.
          </p>

          <div className="home-ctas">
            <button type="button" className="btn btn-primary" onClick={() => navigate('/student/search')}>
              Explorar desafios
            </button>
          </div>

          <div className="trustline">
            <span>Confirme o que você aprendeu</span>
            <i className="line" aria-hidden="true" />
            <span>Repositórios e avaliações de verdade</span>
          </div>
        </div>

        <div className="home-proof">
          <article className="proof-card" aria-label="Exemplo de perfil público">
            <div className="proof-top">
              <div>
                <div className="proof-kicker">perfil público</div>
                <div className="proof-title">Trabalho comprovado</div>
              </div>
              <div className="mini-avatar">P</div>
            </div>

            <div className="activity-grid" aria-hidden="true">
              {activityCells.map((level, index) => (
                <i key={index} className={level} aria-hidden="true" />
              ))}
            </div>

            <div className="activity-legend">
              <span>{resumo}</span>
              <span className="activity-key">
                menos <b></b><b></b><b></b><b></b> mais
              </span>
            </div>
          </article>
        </div>
      </section>

      <section className="home-strip" aria-label="Como funciona o Trampolim">
        <div className="intro">De desafio prático<br />para uma carreira visível.</div>

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
      </section>

      <footer className="home">
        <span>&copy; 2026 Trampolim. Todos os direitos reservados.</span>
      </footer>
    </main>
  )
}
