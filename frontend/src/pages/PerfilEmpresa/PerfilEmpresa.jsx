// Perfil do mentor (rota /business/profile, só mentor): os desafios publicados, as inscrições
// de cada um e os atalhos para publicar, editar, fechar e avaliar.

// IMPORTAÇÕES
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import ContaTopbar from '../../components/ContaTopbar/ContaTopbar';
import EditarPerfil from '../../components/EditarPerfil/EditarPerfil';
import { buscarMeusDados } from '../../services/auth';
import {
  alterarStatusCandidatura,
  alterarStatusDesafio,
  apagarDesafio,
  listarCandidaturasDoDesafio,
  listarMeusDesafios,
} from '../../services/desafios';
import { formatarDia, LABEL_STATUS, listaDeTecnologias } from '../../utils/desafios';
import { iniciais } from '../../utils/iniciais';
import { linkSeguro, semProtocolo } from '../../utils/links';
import './PerfilEmpresa.css';

function situacao(desafio) {
  if (desafio.status === 'FECHADO') return { texto: 'Encerrado', classe: 'status-closed' };
  if (desafio.entregas > 0) return { texto: 'Em andamento', classe: 'status-progress' };
  return { texto: 'Aberto', classe: 'status-open' };
}

export default function PerfilEmpresa() {
  const navigate = useNavigate();

  const [perfil, setPerfil] = useState(null);
  const [desafios, setDesafios] = useState([]);
  const [erroCarregar, setErroCarregar] = useState('');
  const [aviso, setAviso] = useState('');
  const [editando, setEditando] = useState(false);
  const [abertos, setAbertos] = useState({}); // id do desafio -> lista de inscrições carregada

  useEffect(() => {
    let ativo = true;

    Promise.all([buscarMeusDados(), listarMeusDesafios()])
      .then(([dados, lista]) => {
        if (!ativo) return;
        setPerfil(dados);
        setDesafios(lista);
      })
      .catch((err) => {
        if (!ativo) return;
        if (err.status === 401) {
          navigate('/login', { replace: true, state: { from: '/business/profile' } });
          return;
        }
        setErroCarregar('Não consegui carregar seu perfil. Confira se o back-end está rodando.');
      });

    return () => { ativo = false; };
  }, [navigate]);

  // Roda uma ação da API e mostra o erro no topo da lista, se houver
  async function tentar(acao) {
    setAviso('');
    try {
      await acao();
    } catch (err) {
      setAviso(err instanceof TypeError
        ? 'Não consegui falar com o servidor. Confira se o back-end está rodando.'
        : err.message || 'Não foi possível concluir a ação.');
    }
  }

  function alternarInscricoes(desafio) {
    if (abertos[desafio.id]) {
      setAbertos((atual) => {
        const resto = { ...atual };
        delete resto[desafio.id];
        return resto;
      });
      return undefined;
    }
    return tentar(async () => {
      const lista = await listarCandidaturasDoDesafio(desafio.id);
      setAbertos((atual) => ({ ...atual, [desafio.id]: lista }));
    });
  }

  // Aprovar ou recusar uma inscrição: só vale enquanto o aluno não enviou o projeto
  function decidirInscricao(desafio, inscricao, novoStatus) {
    return tentar(async () => {
      await alterarStatusCandidatura(inscricao.id, novoStatus);
      setAbertos((atual) => ({
        ...atual,
        [desafio.id]: (atual[desafio.id] ?? []).map((item) => (
          item.id === inscricao.id ? { ...item, status: novoStatus } : item
        )),
      }));
    });
  }

  function alternarStatus(desafio) {
    const novoStatus = desafio.status === 'FECHADO' ? 'ABERTO' : 'FECHADO';
    return tentar(async () => {
      await alterarStatusDesafio(desafio.id, novoStatus);
      setDesafios((lista) => lista.map((d) => (d.id === desafio.id ? { ...d, status: novoStatus } : d)));
    });
  }

  function apagar(desafio) {
    if (!window.confirm(`Apagar o desafio "${desafio.titulo}"? Essa ação não pode ser desfeita.`)) return undefined;
    return tentar(async () => {
      await apagarDesafio(desafio.id);
      setDesafios((lista) => lista.filter((d) => d.id !== desafio.id));
    });
  }

  function aoSalvar(salvo) {
    setPerfil(salvo);
    setEditando(false);
  }

  const totalInscritos = desafios.reduce((total, desafio) => total + desafio.inscritos, 0);
  const totalEntregas = desafios.reduce((total, desafio) => total + desafio.entregas, 0);
  const linkPerfil = linkSeguro(perfil?.linkedin) ?? linkSeguro(perfil?.github);

  return (
    <main className="surface-paper">
      <header className="topbar">
        <div className="crumb">
          TRAMPOLIM / <b>PERFIL DO MENTOR</b>
        </div>

        <div className="top-actions">
          <button
            type="button"
            className="text-button"
            onClick={() => navigate('/student/search')}
          >
            Explorar desafios
          </button>
          <ContaTopbar />
        </div>
      </header>

      {erroCarregar && <div className="empty-state" role="alert">{erroCarregar}</div>}

      {!perfil && !erroCarregar && <div className="desafios-loading">Carregando perfil…</div>}

      {perfil && (
        <>
          <section className="company-banner">
            <div className="company-logo" aria-hidden="true">
              {iniciais(perfil.name)}
            </div>

            <div className="company-info">
              <h1>{perfil.name}</h1>
              <div className="company-eyebrow">
                {perfil.professionalTitle || 'MENTOR · TRAMPOLIM'}
              </div>
              <p>{perfil.bio || 'Conte aos alunos quem é você e no que atua: clique em "Editar perfil".'}</p>
              {linkPerfil && (
                <a href={linkPerfil} target="_blank" rel="noreferrer" className="company-site">
                  {semProtocolo(linkPerfil)} ↗
                </a>
              )}
            </div>

            <div className="company-banner-actions">
              <button type="button" className="btn btn-primary" onClick={() => navigate('/business/post-challenge')}>
                Publicar desafio
              </button>
              <button type="button" className="btn btn-outline-light" onClick={() => setEditando(true)}>
                Editar perfil
              </button>
            </div>
          </section>

          {editando && <EditarPerfil perfil={perfil} onSalvo={aoSalvar} onCancelar={() => setEditando(false)} />}

          <div className="company-content">
            <section>
              <div className="company-section-heading">
                <div>
                  <h2>Desafios publicados</h2>
                  <p>Acompanhe as inscrições e avalie as entregas.</p>
                </div>
              </div>

              {aviso && <p className="company-aviso" role="alert">{aviso}</p>}

              {desafios.length === 0 && (
                <div className="company-vazio">
                  <p>Você ainda não publicou nenhum desafio.</p>
                  <button type="button" className="btn btn-primary" onClick={() => navigate('/business/post-challenge')}>
                    Publicar o primeiro desafio
                  </button>
                </div>
              )}

              <div className="company-challenge-list">
                {desafios.map((desafio) => {
                  const taxaEntrega = desafio.inscritos > 0
                    ? Math.round((desafio.entregas / desafio.inscritos) * 100)
                    : 0;
                  const { texto, classe } = situacao(desafio);
                  const inscricoes = abertos[desafio.id];

                  return (
                    <article className="company-challenge-card" key={desafio.id}>
                      <div className="challenge-card-top">
                        <span className="challenge-category">
                          DESAFIO #{String(desafio.id).padStart(2, '0')} · ATÉ {formatarDia(desafio.prazo)}
                        </span>

                        <span className={`challenge-status ${classe}`}>{texto}</span>
                      </div>

                      <h3>{desafio.titulo}</h3>

                      <p>{desafio.descricao}</p>

                      <div className="company-tech-list">
                        {listaDeTecnologias(desafio.tecnologias).map((tecnologia) => (
                          <span className="company-tech-tag" key={tecnologia}>
                            {tecnologia}
                          </span>
                        ))}
                      </div>

                      <div className="challenge-stats">
                        <div className="challenge-stat">
                          <span className="stat-symbol">♙</span>
                          <div>
                            <strong>{desafio.inscritos}</strong>
                            <span> {desafio.inscritos === 1 ? 'inscrito' : 'inscritos'}</span>
                          </div>
                        </div>

                        <div className="challenge-stat">
                          <span className="stat-symbol stat-symbol-green">
                            ✓
                          </span>
                          <div>
                            <strong>{desafio.entregas}</strong>
                            <span> {desafio.entregas === 1 ? 'entregou' : 'entregaram'}</span>
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
                          {desafio.entregas} de {desafio.inscritos} {desafio.inscritos === 1 ? 'entrega' : 'entregas'}
                        </span>

                        <button
                          type="button"
                          className="challenge-link"
                          aria-expanded={Boolean(inscricoes)}
                          onClick={() => alternarInscricoes(desafio)}
                        >
                          {inscricoes ? 'Esconder inscrições' : 'Ver inscrições'}
                        </button>
                      </div>

                      {inscricoes && (
                        <ul className="company-inscricoes">
                          {inscricoes.length === 0 && <li className="company-inscricao-vazia">Ninguém se inscreveu ainda.</li>}
                          {inscricoes.map((inscricao) => (
                            <li key={inscricao.id}>
                              <div>
                                <button
                                  type="button"
                                  className="company-inscrito"
                                  onClick={() => navigate(`/student/profile/${inscricao.candidatoId}`)}
                                >
                                  {inscricao.nomeCandidato}
                                </button>
                                <small>{LABEL_STATUS[inscricao.status] ?? inscricao.status}</small>
                              </div>
                              {inscricao.linkEnvio ? (
                                <button
                                  type="button"
                                  className="challenge-link"
                                  onClick={() => navigate(`/business/assessment/${inscricao.id}`)}
                                >
                                  {inscricao.status === 'AVALIADO' ? 'Ver avaliação' : 'Avaliar'}
                                </button>
                              ) : (
                                <div className="company-decisao">
                                  {inscricao.status !== 'APROVADO' && (
                                    <button
                                      type="button"
                                      className="challenge-link"
                                      aria-label={`Aprovar inscrição de ${inscricao.nomeCandidato}`}
                                      onClick={() => decidirInscricao(desafio, inscricao, 'APROVADO')}
                                    >
                                      Aprovar
                                    </button>
                                  )}
                                  {inscricao.status !== 'REJEITADO' && (
                                    <button
                                      type="button"
                                      className="challenge-link company-apagar"
                                      aria-label={`Recusar inscrição de ${inscricao.nomeCandidato}`}
                                      onClick={() => decidirInscricao(desafio, inscricao, 'REJEITADO')}
                                    >
                                      Recusar
                                    </button>
                                  )}
                                </div>
                              )}
                            </li>
                          ))}
                        </ul>
                      )}

                      <div className="company-acoes">
                        <button type="button" className="challenge-link" onClick={() => navigate(`/student/challenge/${desafio.id}`)}>
                          Ver como aluno
                        </button>
                        <button type="button" className="challenge-link" onClick={() => navigate(`/business/challenge/${desafio.id}/edit`)}>
                          Editar
                        </button>
                        <button type="button" className="challenge-link" onClick={() => alternarStatus(desafio)}>
                          {desafio.status === 'FECHADO' ? 'Reabrir inscrições' : 'Fechar inscrições'}
                        </button>
                        {desafio.inscritos === 0 && (
                          <button type="button" className="challenge-link company-apagar" onClick={() => apagar(desafio)}>
                            Apagar
                          </button>
                        )}
                      </div>
                    </article>
                  );
                })}
              </div>
            </section>

            <section className="company-metrics" aria-label="Indicadores do mentor">
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
                  <strong>{totalInscritos}</strong>
                  <span>INSCRIÇÕES</span>
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
        </>
      )}
    </main>
  );
}
