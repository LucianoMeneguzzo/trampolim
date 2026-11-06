// Responsável: Andrius
// Tela 5 do protótipo: envio do projeto (rota /student/challenge/:id/send-challenge, só aluno).
// O :id é o do desafio; a inscrição do aluno nele vem de GET /api/candidaturas/minhas.

// IMPORTAÇÕES
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import ContaTopbar from '../../components/ContaTopbar/ContaTopbar';
import { enviarProjeto, listarMinhasCandidaturas } from '../../services/desafios';
import { formatarDataHora, LABEL_STATUS } from '../../utils/desafios';
import { linkSeguro, semProtocolo } from '../../utils/links';
import './EnviarDesafio.css';

const ETAPAS = ['Inscrição feita', 'Envie o projeto', 'Avaliação do mentor', 'Portfólio validado'];

// Em qual das ETAPAS a inscrição está (a etapa 4 só vale depois da avaliação)
const ETAPA_POR_STATUS = { PENDENTE: 1, APROVADO: 1, REJEITADO: 1, ENVIADO: 2, AVALIADO: 4 };

export default function EnviarDesafio() {
  const { id } = useParams();
  const navigate = useNavigate();

  // Guarda junto o id carregado: se a URL mudar para outro desafio, o anterior some na hora
  const [carga, setCarga] = useState({ id: null, candidatura: null, erro: '' });
  const carregou = carga.id === id;
  const candidatura = carregou ? carga.candidatura : null;
  const erroCarregar = carregou ? carga.erro : '';

  const [link, setLink] = useState('');
  const [erroLink, setErroLink] = useState('');
  const [erro, setErro] = useState('');
  const [enviando, setEnviando] = useState(false);
  const [recemEnviado, setRecemEnviado] = useState(false);

  useEffect(() => {
    let ativo = true;

    listarMinhasCandidaturas()
      .then((lista) => {
        if (!ativo) return;
        const minha = lista.find((c) => String(c.desafioId) === id) ?? null;
        setCarga({ id, candidatura: minha, erro: '' });
        setLink(minha?.linkEnvio ? semProtocolo(minha.linkEnvio) : '');
      })
      .catch((err) => {
        if (!ativo) return;
        if (err.status === 401) {
          navigate('/login', { replace: true, state: { from: `/student/challenge/${id}/send-challenge` } });
          return;
        }
        setCarga({ id, candidatura: null, erro: 'Não consegui carregar sua inscrição. Confira se o back-end está rodando.' });
      });

    return () => { ativo = false; };
  }, [id, navigate]);

  async function whenSubmit(event) {
    event.preventDefault();
    setErro('');
    setErroLink('');
    setEnviando(true);

    try {
      const atualizada = await enviarProjeto(candidatura.id, link.trim());
      setCarga((atual) => ({ ...atual, candidatura: atualizada }));
      setLink(semProtocolo(atualizada.linkEnvio));
      setRecemEnviado(true);
    } catch (err) {
      setRecemEnviado(false);
      if (err.status === 401) {
        navigate('/login', { replace: true, state: { from: `/student/challenge/${id}/send-challenge` } });
        return;
      }
      if (err instanceof TypeError) {
        setErro('Não consegui falar com o servidor. Confira se o back-end está rodando.');
      } else if (err.campos?.linkEnvio) {
        setErroLink(err.campos.linkEnvio);
      } else {
        setErro(err.message || 'Não foi possível enviar agora. Tente de novo em instantes.');
      }
    } finally {
      setEnviando(false);
    }
  }

  const status = candidatura?.status;
  const etapa = ETAPA_POR_STATUS[status] ?? 0;
  const enviado = Boolean(candidatura?.linkEnvio);
  const validadoNoGitHub = Boolean(candidatura?.repoLinguagem || candidatura?.repoUltimoCommit);

  return (
    <div className="surface-paper">
      <header className="topbar">
        <div className="crumb">
          MINHAS ENTREGAS / <b>{candidatura ? candidatura.tituloDesafio.toUpperCase() : 'ENVIO'}</b>
        </div>

        <div className="top-actions">
          {status && <span className="status amber-status">{LABEL_STATUS[status] ?? status}</span>}
          <ContaTopbar />
        </div>
      </header>

      <div className="page-pad">
        <div className="page-header">
          <div>
            <h1>Envie seu projeto</h1>

            <p>
              Quando você enviar, vamos validar o repositório e avisar o
              mentor que sua entrega está pronta para avaliação.
            </p>
          </div>
        </div>

        {erroCarregar && <div className="empty-state" role="alert">{erroCarregar}</div>}

        {!carregou && <div className="desafios-loading">Carregando sua inscrição…</div>}

        {carregou && !erroCarregar && !candidatura && (
          <div className="empty-state">
            <p>Você ainda não se inscreveu neste desafio.</p>
            <button type="button" className="btn btn-primary" onClick={() => navigate(`/student/challenge/${id}`)}>
              Ver o desafio
            </button>
          </div>
        )}

        {candidatura && (
          <>
            <div className="workflow" aria-label="Etapas da entrega">
              {ETAPAS.map((nome, indice) => {
                let classe = '';
                if (indice < etapa) classe = 'done';
                else if (indice === etapa) classe = 'active';
                return <div key={nome} className={`workflow-step ${classe}`}>{nome}</div>;
              })}
            </div>

            <div className="send-layout">
              {status === 'AVALIADO' ? (
                <section className="send-card">
                  <h2>Seu projeto foi avaliado</h2>
                  <p>Por {candidatura.avaliacao?.nomeMentor}, em {formatarDataHora(candidatura.avaliacao?.dataAvaliacao)}.</p>
                  <div className="send-nota">{candidatura.avaliacao?.nota}<small>/10</small></div>
                  <p className="send-feedback">{candidatura.avaliacao?.feedback}</p>
                  <button type="button" className="btn btn-primary" onClick={() => navigate('/student/profile')}>
                    Ver no meu portfólio <span className="arrow">→</span>
                  </button>
                </section>
              ) : status === 'REJEITADO' ? (
                <section className="send-card">
                  <h2>Inscrição não aprovada</h2>
                  <p>O mentor não aprovou esta inscrição, então ela não aceita envio de projeto.</p>
                  <button type="button" className="btn btn-primary" onClick={() => navigate('/student/search')}>
                    Ver outros desafios <span className="arrow">→</span>
                  </button>
                </section>
              ) : (
                <form className="send-card" onSubmit={whenSubmit}>
                  <h2>Link do repositório</h2>

                  <p>
                    Use um repositório público no GitHub. A validação confirma o
                    projeto, a linguagem principal e seu último commit.
                  </p>

                  {erro && <p className="send-erro" role="alert">{erro}</p>}

                  <div className="field">
                    <label htmlFor="repo-url">URL do GitHub</label>

                    <div className="repo-input">
                      <span>⌘</span>

                      <input
                        id="repo-url"
                        type="text"
                        value={link}
                        onChange={(e) => setLink(e.target.value)}
                        placeholder="github.com/usuario/projeto"
                        aria-describedby="repo-valid"
                        required
                      />
                    </div>

                    <div id="repo-valid">
                      {erroLink && <div className="invalid-message" role="alert">{erroLink}</div>}
                      {!erroLink && recemEnviado && (
                        <div className="valid-message"><i>✓</i> Projeto enviado para o mentor.</div>
                      )}
                    </div>
                  </div>

                  {enviado && (
                    <>
                      <div className="repo-data" aria-label="Dados do repositório enviado">
                        <div>
                          <span>Linguagem principal</span>
                          <strong>{candidatura.repoLinguagem ?? '—'}</strong>
                        </div>

                        <div>
                          <span>Último commit</span>
                          <strong>{formatarDataHora(candidatura.repoUltimoCommit)}</strong>
                        </div>

                        <div>
                          <span>Enviado em</span>
                          <strong>{formatarDataHora(candidatura.dataEnvio)}</strong>
                        </div>
                      </div>

                      {!validadoNoGitHub && (
                        <p className="send-aviso">
                          Não consegui consultar os dados no GitHub agora. O link foi salvo e o mentor já pode ver.
                        </p>
                      )}

                      {linkSeguro(candidatura.linkEnvio) && (
                        <a className="send-repo" href={linkSeguro(candidatura.linkEnvio)} target="_blank" rel="noreferrer">
                          Abrir repositório ↗
                        </a>
                      )}
                    </>
                  )}

                  <button type="submit" className="btn btn-primary" disabled={enviando}>
                    {enviando ? 'Validando…' : (enviado ? 'Atualizar envio' : 'Enviar para avaliação')} <span className="arrow">→</span>
                  </button>
                </form>
              )}

              <aside className="send-help">
                <div className="micro-label">
                  Antes de enviar
                </div>

                <h3>Uma entrega que se explica</h3>

                <p>
                  O mentor vai olhar o projeto por dentro. Deixe os principais
                  caminhos fáceis de encontrar.
                </p>

                <div className="tip-list">
                  <div className="tip">
                    <span className="tip-dot"></span>
                    <span>
                      README com contexto e como rodar.
                    </span>
                  </div>

                  <div className="tip">
                    <span className="tip-dot"></span>
                    <span>
                      Commits organizados e mensagem clara.
                    </span>
                  </div>

                  <div className="tip">
                    <span className="tip-dot"></span>
                    <span>
                      Variáveis sensíveis fora do repositório.
                    </span>
                  </div>
                </div>
              </aside>
            </div>
          </>
        )}
      </div>
    </div>
  );
}
