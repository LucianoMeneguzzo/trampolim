// Tela 4 do protótipo: detalhe do desafio (rota /student/challenge/:id, aberta sem login)

import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import ContaTopbar from '../../components/ContaTopbar/ContaTopbar'
import { getUser } from '../../services/api'
import { buscarDesafio, inscrever, listarMinhasCandidaturas } from '../../services/desafios'
import { formatarDia, LABEL_NIVEL, listaDeTecnologias, prazoVencido } from '../../utils/desafios'
import { iniciais } from '../../utils/iniciais'
import './DetalheDesafio.css'

export default function DetalheDesafio() {
  const { id } = useParams()
  const navigate = useNavigate()
  const usuario = getUser()
  const ehAluno = usuario?.type === 'STUDENT'

  // Guarda junto o id carregado: se a URL mudar para outro desafio, o anterior some na hora
  const [carga, setCarga] = useState({ id: null, desafio: null, minha: null, erro: '' })
  const desafio = carga.id === id ? carga.desafio : null
  const minha = carga.id === id ? carga.minha : null
  const erroCarregar = carga.id === id ? carga.erro : ''

  const [erro, setErro] = useState('')
  const [inscrevendo, setInscrevendo] = useState(false)

  useEffect(() => {
    let ativo = true
    // A inscrição do aluno é um complemento: se a consulta falhar, o desafio aparece mesmo assim
    const minhas = ehAluno ? listarMinhasCandidaturas().catch(() => []) : Promise.resolve([])

    Promise.all([buscarDesafio(id), minhas])
      .then(([dados, inscricoes]) => {
        if (!ativo) return
        setCarga({ id, desafio: dados, minha: inscricoes.find(c => c.desafioId === dados.id) ?? null, erro: '' })
      })
      .catch(err => {
        if (!ativo) return
        const mensagem = err.status === 404
          ? 'Este desafio não existe ou foi removido.'
          : 'Não consegui carregar o desafio. Confira se o back-end está rodando.'
        setCarga({ id, desafio: null, minha: null, erro: mensagem })
      })

    return () => { ativo = false }
  }, [id, ehAluno])

  async function handleInscrever() {
    setErro('')
    setInscrevendo(true)
    try {
      await inscrever(desafio.id)
      navigate(`/student/challenge/${id}/send-challenge`)
    } catch (err) {
      if (err.status === 401) {
        navigate('/login', { state: { from: `/student/challenge/${id}` } })
        return
      }
      setErro(err instanceof TypeError
        ? 'Não consegui falar com o servidor. Confira se o back-end está rodando.'
        : err.message || 'Não foi possível fazer a inscrição.')
      setInscrevendo(false)
    }
  }

  function motivoSemInscricao() {
    if (desafio.status === 'FECHADO') return 'As inscrições deste desafio estão fechadas.'
    if (prazoVencido(desafio.prazo)) return 'O prazo deste desafio já terminou.'
    if (desafio.vagas != null && desafio.inscritos >= desafio.vagas) return 'As vagas deste desafio acabaram.'
    return null
  }

  function acao() {
    const motivo = motivoSemInscricao()

    if (usuario && !ehAluno) {
      return desafio.criadorId === usuario.userId
        ? <button type="button" className="btn btn-primary" onClick={() => navigate('/business/profile')}>Gerenciar no meu perfil</button>
        : <p className="detalhe-nota">As inscrições são para contas de aluno.</p>
    }

    if (minha) {
      if (minha.status === 'REJEITADO') return <p className="detalhe-nota">Sua inscrição neste desafio não foi aprovada.</p>
      const texto = { ENVIADO: 'Ver minha entrega', AVALIADO: 'Ver minha avaliação' }[minha.status] ?? 'Enviar meu projeto'
      return (
        <button type="button" className="btn btn-primary" onClick={() => navigate(`/student/challenge/${id}/send-challenge`)}>
          {texto} <span className="arrow" aria-hidden="true">→</span>
        </button>
      )
    }

    if (motivo) return <p className="detalhe-nota">{motivo}</p>

    if (!usuario) {
      return (
        <>
          <button type="button" className="btn btn-primary" onClick={() => navigate('/login', { state: { from: `/student/challenge/${id}` } })}>
            Entrar para se inscrever <span className="arrow" aria-hidden="true">→</span>
          </button>
          <p className="detalhe-nota">Você poderá enviar o link do repositório depois de se inscrever.</p>
        </>
      )
    }

    return (
      <>
        <button type="button" className="btn btn-primary" onClick={handleInscrever} disabled={inscrevendo}>
          {inscrevendo ? 'Inscrevendo…' : 'Inscrever-me'} <span className="arrow" aria-hidden="true">→</span>
        </button>
        <p className="detalhe-nota">Você poderá enviar o link do repositório depois de se inscrever.</p>
      </>
    )
  }

  return (
    <div className="surface-paper">
      <header className="topbar">
        <div className="crumb">
          DESAFIOS / <b>{desafio ? desafio.titulo.toUpperCase() : 'DETALHE'}</b>
        </div>
        <div className="top-actions">
          <button type="button" className="plain-link" onClick={() => navigate('/student/search')}>Todos os desafios</button>
          <ContaTopbar />
        </div>
      </header>

      <div className="detalhe-pad">
        {erroCarregar && (
          <div className="empty-state" role="alert">
            <p>{erroCarregar}</p>
            <button type="button" className="btn btn-primary" onClick={() => navigate('/student/search')}>Ver todos os desafios</button>
          </div>
        )}

        {!desafio && !erroCarregar && <div className="desafios-loading">Carregando desafio…</div>}

        {desafio && (
          <div className="detalhe-layout">
            <article className="detalhe-main">
              <button type="button" className="detalhe-voltar" onClick={() => navigate('/student/search')}>← Voltar para desafios</button>

              <div className="detalhe-tags">
                <span className={`detalhe-chip ${desafio.status === 'FECHADO' ? 'fechado' : 'aberto'}`}>
                  {desafio.status === 'FECHADO' ? 'Fechado' : 'Aberto'}
                </span>
                <span className="detalhe-chip nivel">{LABEL_NIVEL[desafio.nivel] ?? desafio.nivel}</span>
              </div>

              <h1>{desafio.titulo}</h1>

              <section className="detalhe-secao">
                <h2>O contexto</h2>
                <p>{desafio.descricao}</p>
              </section>

              <section className="detalhe-secao">
                <h2>Tecnologias sugeridas</h2>
                <div className="detalhe-tags">
                  {listaDeTecnologias(desafio.tecnologias).map(t => <span key={t} className="detalhe-chip">{t}</span>)}
                </div>
              </section>
            </article>

            <aside className="detalhe-lado">
              <h3>Antes de começar</h3>
              <dl className="detalhe-fatos">
                <div><dt>Nível</dt><dd>{LABEL_NIVEL[desafio.nivel] ?? desafio.nivel}</dd></div>
                <div><dt>Prazo</dt><dd>{formatarDia(desafio.prazo)}</dd></div>
                <div><dt>Inscritos</dt><dd>{desafio.inscritos ?? 0} de {desafio.vagas} vagas</dd></div>
                <div><dt>Publicação</dt><dd>{formatarDia(desafio.dataCriacao)}</dd></div>
                {desafio.empresa && <div><dt>Empresa</dt><dd>{desafio.empresa}</dd></div>}
              </dl>

              <div className="detalhe-mentor">
                <span className="detalhe-mentor-foto">{iniciais(desafio.nomeCriador)}</span>
                <div>
                  <small>Mentor responsável</small>
                  <strong>{desafio.nomeCriador}</strong>
                </div>
              </div>

              {erro && <p className="detalhe-erro" role="alert">{erro}</p>}

              {acao()}
            </aside>
          </div>
        )}
      </div>
    </div>
  )
}
