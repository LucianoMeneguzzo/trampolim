// Responsável: Rogério (pareado com o Ismael pra entender a lógica por trás)
// Tela 7 do protótipo: "Avalie a entrega" (rota /business/assessment/:id, só mentor).
// O :id é o id da candidatura (inscrição do aluno no desafio).

// IMPORTAÇÕES
import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import AreaTopbar from '../../components/AreaTopbar/AreaTopbar'
import { avaliarCandidatura, buscarCandidatura } from '../../services/desafios'
import { iniciais } from '../../utils/iniciais'
import './AvaliarDesafio.css'

const FEEDBACK_MIN = 20
const FEEDBACK_MAX = 3000

const MSG_CARREGAMENTO = {
  403: 'Esta entrega pertence a um desafio de outro mentor.',
  404: 'Não encontramos esta entrega. Confira o link.',
  400: 'Endereço de entrega inválido.',
}

function diasEntre(inicio, fim = new Date()) {
  if (!inicio) return null
  const ms = new Date(fim) - new Date(inicio)
  return Math.max(0, Math.floor(ms / 86400000))
}

function textoDias(dias) {
  if (dias === null) return '—'
  if (dias === 0) return 'hoje'
  return dias === 1 ? '1 dia' : `${dias} dias`
}

function textoInscricao(dias) {
  if (dias === null) return ''
  return dias === 0 ? 'feita hoje' : `há ${textoDias(dias)}`
}

function formatarData(data) {
  if (!data) return '—'
  return new Date(data).toLocaleString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

function semProtocolo(link) {
  return link.replace(/^https?:\/\//, '')
}

export default function AvaliarDesafio() {
  const { id } = useParams()
  const navigate = useNavigate()

  // Guarda junto o id carregado: se a URL mudar para outra entrega, a anterior some na hora
  const [carga, setCarga] = useState({ id: null, entrega: null, erro: '' })
  const entrega = carga.id === id ? carga.entrega : null
  const erroCarregar = carga.id === id ? carga.erro : ''

  const [nota, setNota] = useState(7)
  const [feedback, setFeedback] = useState('')
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    let ativo = true

    buscarCandidatura(id)
      .then(dados => { if (ativo) setCarga({ id, entrega: dados, erro: '' }) })
      .catch(err => {
        if (!ativo) return
        if (err.status === 401) {
          navigate('/login', { replace: true, state: { from: `/business/assessment/${id}` } })
          return
        }
        const mensagem = err instanceof TypeError
          ? 'Não consegui falar com o servidor. Confira se o back-end está rodando.'
          : MSG_CARREGAMENTO[err.status] ?? 'Não foi possível carregar a entrega.'
        setCarga({ id, entrega: null, erro: mensagem })
      })

    return () => { ativo = false }
  }, [id, navigate])

  async function whenSubmit(e) {
    e.preventDefault()
    setErro('')
    setEnviando(true)

    try {
      const avaliacao = await avaliarCandidatura(id, { nota, feedback: feedback.trim() })
      setCarga(atual => ({ ...atual, entrega: { ...atual.entrega, status: 'AVALIADO', avaliacao } }))
    } catch (err) {
      if (err.status === 401) {
        navigate('/login', { replace: true, state: { from: `/business/assessment/${id}` } })
        return
      }
      setErro(err instanceof TypeError
        ? 'Não consegui falar com o servidor. Confira se o back-end está rodando.'
        : err.message || 'Não foi possível publicar a avaliação.')
    } finally {
      setEnviando(false)
    }
  }

  const primeiroNome = entrega?.nomeCandidato?.split(' ')[0] ?? 'o aluno'
  const avaliacao = entrega?.avaliacao
  const semEnvio = entrega && !entrega.linkRepositorio
  const feedbackCurto = feedback.trim().length < FEEDBACK_MIN

  let status = 'Carregando'
  if (avaliacao) status = 'Avaliada'
  else if (semEnvio) status = 'Aguardando envio'
  else if (entrega) status = 'Entrega nova'

  return (
    <div className="area-page">
      <AreaTopbar pagina="AVALIAR ENTREGA">
        {entrega && <span className={`area-chip ${avaliacao ? 'sage' : ''}`}>{status}</span>}
      </AreaTopbar>

      <div className="area-pad">
        <div className="area-header">
          <div>
            <h1>Avalie a entrega</h1>
            <p>Sua avaliação vai para a pessoa e também vira uma evidência no perfil público dela.</p>
          </div>
        </div>

        {erroCarregar && (
          <div className="area-empty" role="alert">
            <h2>Entrega indisponível</h2>
            <p>{erroCarregar}</p>
            <button type="button" className="btn btn-dark" onClick={() => navigate('/business/post-challenge')}>
              Voltar para a área do mentor
            </button>
          </div>
        )}

        {!entrega && !erroCarregar && <div className="area-empty">Carregando entrega…</div>}

        {entrega && (
          <div className="review-layout">
            <article className="submission-card">
              <div className="submission-user">
                <span className="mentor-image">{iniciais(entrega.nomeCandidato)}</span>
                <div>
                  <h2>{entrega.nomeCandidato}</h2>
                  <p>Inscrição em “{entrega.tituloDesafio}” {textoInscricao(diasEntre(entrega.dataCandidatura))}</p>
                </div>
              </div>

              <div className="repo-preview">
                {entrega.linkRepositorio ? (
                  <>
                    <div>
                      <strong>{semProtocolo(entrega.linkRepositorio)}</strong>
                      <span>Enviado em {formatarData(entrega.dataEnvio)}</span>
                    </div>
                    <a className="profile-link" href={entrega.linkRepositorio} target="_blank" rel="noreferrer">Abrir repo ↗</a>
                  </>
                ) : (
                  <div>
                    <strong>Projeto ainda não enviado</strong>
                    <span>A avaliação libera quando {primeiroNome} enviar o link do repositório.</span>
                  </div>
                )}
              </div>

              <div className="review-facts">
                <div className="review-fact"><span>Linguagem</span><strong>{entrega.repoLinguagem ?? '—'}</strong></div>
                <div className="review-fact"><span>Último commit</span><strong>{formatarData(entrega.repoUltimoCommit)}</strong></div>
                <div className="review-fact">
                  <span>Tempo de entrega</span>
                  <strong>{entrega.dataEnvio ? textoDias(diasEntre(entrega.dataCandidatura, entrega.dataEnvio)) : '—'}</strong>
                </div>
              </div>
            </article>

            {avaliacao ? (
              <section className="review-form" aria-live="polite">
                <h2>Avaliação publicada</h2>
                <p>Por {avaliacao.nomeMentor} em {formatarData(avaliacao.dataAvaliacao)}.</p>
                <div className="score-wrap">
                  <div className="score-value">{Number(avaliacao.nota).toFixed(1)}<small>/10</small></div>
                  <div className="score-control"><span className="score-label">Nota atribuída</span></div>
                </div>
                <p className="review-feedback">{avaliacao.feedback}</p>
              </section>
            ) : (
              <form className="review-form" onSubmit={whenSubmit}>
                <h2>Seu feedback</h2>
                <p>Seja específico sobre o que funcionou e qual seria o próximo passo de evolução.</p>

                <div className="score-wrap">
                  <div className="score-value" aria-hidden="true">{nota.toFixed(1)}<small>/10</small></div>
                  <div className="score-control">
                    <label htmlFor="score-input">Nota atribuída</label>
                    <input id="score-input" type="range" min="0" max="10" step="0.5" value={nota}
                      onChange={e => setNota(Number(e.target.value))} disabled={semEnvio}
                      aria-valuetext={`${nota.toFixed(1)} de 10`} />
                  </div>
                </div>

                <div className="field">
                  <label htmlFor="feedback">Feedback para {primeiroNome}</label>
                  <textarea id="feedback" value={feedback} onChange={e => setFeedback(e.target.value)}
                    minLength={FEEDBACK_MIN} maxLength={FEEDBACK_MAX} required disabled={semEnvio}
                    placeholder="O que ficou bom, o que pode evoluir e por onde começar." />
                  <span className="feedback-count">
                    {feedbackCurto ? `Mínimo de ${FEEDBACK_MIN} caracteres` : `${feedback.trim().length}/${FEEDBACK_MAX}`}
                  </span>
                </div>

                {erro && <p className="area-alert" role="alert">{erro}</p>}

                <button type="submit" className="btn btn-primary" disabled={semEnvio || enviando || feedbackCurto}>
                  {enviando ? 'Publicando…' : `Publicar avaliação para ${primeiroNome}`} <span className="arrow" aria-hidden="true">→</span>
                </button>
                <p className="review-note">A nota e o feedback passam a aparecer no perfil público de {primeiroNome}.</p>
              </form>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
