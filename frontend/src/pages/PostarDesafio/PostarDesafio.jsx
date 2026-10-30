// Tela 6 do protótipo: "Publique um desafio" (rota /business/post-challenge, só mentor)

// IMPORTAÇÕES
import { useState } from 'react'
import { useNavigate } from 'react-router'
import AreaTopbar from '../../components/AreaTopbar/AreaTopbar'
import { criarDesafio, NIVEIS } from '../../services/desafios'
import './PostarDesafio.css'

const PRAZOS = [7, 10, 14]
const MAX_TECNOLOGIAS = 10

const FORM_INICIAL = {
  titulo: '',
  descricao: '',
  nivel: 'INTERMEDIARIO',
  prazoDias: 7,
  vagas: 20,
  empresa: '',
}

// Os erros de validação do back vêm com o nome do campo da API (title, level...)
const CAMPO_DA_API = {
  title: 'titulo',
  description: 'descricao',
  level: 'nivel',
  technologies: 'tecnologias',
  deadlineDays: 'prazoDias',
  slots: 'vagas',
  companyName: 'empresa',
}

export default function PostarDesafio() {
  const navigate = useNavigate()
  const [form, setForm] = useState(FORM_INICIAL)
  const [tecnologias, setTecnologias] = useState([])
  const [novaTecnologia, setNovaTecnologia] = useState('')
  const [errosCampos, setErrosCampos] = useState({})
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)
  const [publicado, setPublicado] = useState(null)

  function refreshForm(e) {
    const { id, value } = e.target
    setForm(f => ({ ...f, [id]: value }))
  }

  function adicionarTecnologia() {
    const nome = novaTecnologia.trim().replace(/,$/, '')
    if (!nome) return
    const jaExiste = tecnologias.some(t => t.toLowerCase() === nome.toLowerCase())
    if (!jaExiste && tecnologias.length < MAX_TECNOLOGIAS) {
      setTecnologias(lista => [...lista, nome])
    }
    setNovaTecnologia('')
  }

  function removerTecnologia(nome) {
    setTecnologias(lista => lista.filter(t => t !== nome))
  }

  // Enter ou vírgula adicionam a tecnologia; Backspace no campo vazio remove a última
  function teclaNaTecnologia(e) {
    if (e.key === 'Enter' || e.key === ',') {
      e.preventDefault()
      adicionarTecnologia()
    } else if (e.key === 'Backspace' && !novaTecnologia && tecnologias.length) {
      setTecnologias(lista => lista.slice(0, -1))
    }
  }

  async function whenSubmit(e) {
    e.preventDefault()
    setErro('')
    setErrosCampos({})
    setEnviando(true)

    try {
      const desafio = await criarDesafio({
        ...form,
        titulo: form.titulo.trim(),
        descricao: form.descricao.trim(),
        tecnologias,
        prazoDias: Number(form.prazoDias),
        vagas: Number(form.vagas),
        empresa: form.empresa.trim(),
      })
      setPublicado(desafio)
    } catch (err) {
      if (err.status === 401) {
        navigate('/login', { replace: true, state: { from: '/business/post-challenge' } })
        return
      }
      if (err instanceof TypeError) {
        setErro('Não consegui falar com o servidor. Confira se o back-end está rodando.')
      } else if (err.status === 403) {
        setErro('Só contas de mentor podem publicar desafios.')
      } else if (Object.keys(err.campos ?? {}).length) {
        const porCampo = {}
        Object.entries(err.campos).forEach(([campo, msg]) => { porCampo[CAMPO_DA_API[campo] ?? campo] = msg })
        setErrosCampos(porCampo)
        setErro('Confira os campos destacados.')
      } else {
        setErro(err.message || 'Não foi possível publicar agora. Tente de novo em instantes.')
      }
    } finally {
      setEnviando(false)
    }
  }

  function publicarOutro() {
    setForm(FORM_INICIAL)
    setTecnologias([])
    setPublicado(null)
  }

  return (
    <div className="area-page">
      <AreaTopbar pagina="NOVO DESAFIO">
        <button type="button" className="area-link" onClick={() => navigate('/desafios')}>Ver desafios</button>
      </AreaTopbar>

      <div className="area-pad">
        <div className="area-header">
          <div>
            <h1>Publique um desafio</h1>
            <p>Um bom desafio parte de um contexto real e deixa claro o que a pessoa precisa entregar.</p>
          </div>
          <span className={`area-chip ${publicado ? 'sage' : ''}`}>{publicado ? 'PUBLICADO' : 'RASCUNHO'}</span>
        </div>

        <div className="create-layout">
          {publicado ? (
            <section className="form-card publish-success" aria-live="polite">
              <div className="micro-label">Desafio publicado</div>
              <h2>{publicado.titulo}</h2>
              <p>
                Já está aberto para inscrições
                {publicado.prazoDias ? `, com prazo de ${publicado.prazoDias} dias` : ''}
                {publicado.vagas ? ` e ${publicado.vagas} vagas` : ''}.
              </p>
              {publicado.tecnologias?.length > 0 && (
                <div className="chips-list">
                  {publicado.tecnologias.map(t => <span key={t} className="area-chip">{t}</span>)}
                </div>
              )}
              <div className="publish-actions">
                <button type="button" className="btn btn-dark" onClick={publicarOutro}>Publicar outro desafio</button>
                <button type="button" className="btn btn-outline-paper" onClick={() => navigate('/desafios')}>Ver lista de desafios</button>
              </div>
            </section>
          ) : (
            <form className="form-card" onSubmit={whenSubmit}>
              {erro && <p className="area-alert" role="alert">{erro}</p>}

              <div className="field">
                <label htmlFor="titulo">Título do desafio</label>
                <input id="titulo" value={form.titulo} onChange={refreshForm} maxLength={150} required
                  placeholder="Ex.: API de pedidos para uma pequena loja" />
                {errosCampos.titulo && <span className="field-error">{errosCampos.titulo}</span>}
              </div>

              <div className="field">
                <label htmlFor="descricao">Contexto e objetivo</label>
                <textarea id="descricao" value={form.descricao} onChange={refreshForm} required
                  placeholder="Qual problema existe, para quem, e o que a entrega precisa resolver?" />
                {errosCampos.descricao && <span className="field-error">{errosCampos.descricao}</span>}
              </div>

              <div className="field">
                <label htmlFor="tecnologia">Tecnologias</label>
                <div className="chips-input">
                  {tecnologias.map(t => (
                    <button key={t} type="button" className="area-chip chip-remove" onClick={() => removerTecnologia(t)}
                      aria-label={`Remover ${t}`}>
                      {t} <span aria-hidden="true">×</span>
                    </button>
                  ))}
                  <input id="tecnologia" value={novaTecnologia} onChange={e => setNovaTecnologia(e.target.value)}
                    onKeyDown={teclaNaTecnologia} onBlur={adicionarTecnologia} maxLength={40}
                    placeholder={tecnologias.length < MAX_TECNOLOGIAS ? 'Adicionar e apertar Enter' : 'Limite atingido'}
                    disabled={tecnologias.length >= MAX_TECNOLOGIAS} />
                </div>
                {errosCampos.tecnologias && <span className="field-error">{errosCampos.tecnologias}</span>}
              </div>

              <div className="field-row thirds">
                <div className="field">
                  <label htmlFor="nivel">Nível</label>
                  <select id="nivel" value={form.nivel} onChange={refreshForm}>
                    {NIVEIS.map(n => <option key={n.valor} value={n.valor}>{n.label}</option>)}
                  </select>
                  {errosCampos.nivel && <span className="field-error">{errosCampos.nivel}</span>}
                </div>
                <div className="field">
                  <label htmlFor="prazoDias">Prazo</label>
                  <select id="prazoDias" value={form.prazoDias} onChange={refreshForm}>
                    {PRAZOS.map(p => <option key={p} value={p}>{p} dias</option>)}
                  </select>
                  {errosCampos.prazoDias && <span className="field-error">{errosCampos.prazoDias}</span>}
                </div>
                <div className="field">
                  <label htmlFor="vagas">Vagas</label>
                  <input id="vagas" type="number" min={1} max={500} value={form.vagas} onChange={refreshForm} required />
                  {errosCampos.vagas && <span className="field-error">{errosCampos.vagas}</span>}
                </div>
              </div>

              <div className="field">
                <label htmlFor="empresa">
                  Em nome de uma empresa <span className="label-optional">(opcional)</span>
                </label>
                <input id="empresa" value={form.empresa} onChange={refreshForm} maxLength={150} placeholder="Ex.: Loja Horizonte" />
                {errosCampos.empresa && <span className="field-error">{errosCampos.empresa}</span>}
              </div>

              <button type="submit" className="btn btn-dark" disabled={enviando}>
                {enviando ? 'Publicando…' : 'Publicar desafio'} <span className="arrow" aria-hidden="true">→</span>
              </button>
            </form>
          )}

          <aside className="create-aside">
            <div className="publish-guide">
              <h3>Antes de publicar</h3>
              <div className="guide-item"><span className="guide-number">1</span><span>Explique qual problema existe antes de listar tecnologias.</span></div>
              <div className="guide-item"><span className="guide-number">2</span><span>Defina entregáveis que possam ser verificados no repositório.</span></div>
              <div className="guide-item"><span className="guide-number">3</span><span>Escolha um prazo proporcional ao nível.</span></div>
            </div>
            <p className="draft-note">Ao publicar, o desafio fica aberto para inscrições na lista de desafios.</p>
          </aside>
        </div>
      </div>
    </div>
  )
}
