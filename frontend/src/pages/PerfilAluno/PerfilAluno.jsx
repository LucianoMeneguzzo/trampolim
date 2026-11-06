// Tela 8 do protótipo: perfil do aluno, o portfólio validado.
//   /student/profile/:id  pública, abre sem login (é o link que a pessoa compartilha)
//   /student/profile      o próprio perfil, com edição e as inscrições em andamento

// IMPORTAÇÕES
import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router';
import ContaTopbar from '../../components/ContaTopbar/ContaTopbar';
import EditarPerfil from '../../components/EditarPerfil/EditarPerfil';
import { getUser } from '../../services/api';
import { listarMinhasCandidaturas } from '../../services/desafios';
import { buscarPerfilPublico } from '../../services/usuarios';
import { LABEL_STATUS, listaDeTecnologias } from '../../utils/desafios';
import { iniciais } from '../../utils/iniciais';
import { linkSeguro, semProtocolo } from '../../utils/links';
import './PerfilAluno.css';

const QUADRADOS = 30;
const DIAS_POR_QUADRADO = 6; // 30 quadrados de 6 dias: os últimos 6 meses

// Quantas entregas caíram em cada quadrado, do mais antigo para o mais recente
function ritmoDeEntregas(trabalhos) {
  const quadrados = Array(QUADRADOS).fill(0);
  trabalhos.forEach((t) => {
    const dias = Math.floor((Date.now() - new Date(t.dataEnvio).getTime()) / 86400000);
    const indice = QUADRADOS - 1 - Math.floor(dias / DIAS_POR_QUADRADO);
    if (indice >= 0 && indice < QUADRADOS) quadrados[indice] += 1;
  });
  return quadrados;
}

function resumo(trabalhos) {
  const tecnologias = new Set(trabalhos.flatMap((t) => listaDeTecnologias(t.tecnologias)));
  const soma = trabalhos.reduce((total, t) => total + t.nota, 0);
  return {
    desafios: trabalhos.length,
    media: trabalhos.length ? (soma / trabalhos.length).toFixed(1) : '—',
    tecnologias: tecnologias.size,
  };
}

export default function PerfilAluno() {
  const { id } = useParams();
  const navigate = useNavigate();
  const usuario = getUser();

  const alvo = String(id ?? usuario?.userId);
  const meuPerfil = usuario?.type === 'STUDENT' && String(usuario.userId) === alvo;

  // Guarda junto o perfil carregado: se a URL mudar para outra pessoa, o anterior some na hora
  const [carga, setCarga] = useState({ alvo: null, perfil: null, inscricoes: [], erro: '' });
  const perfil = carga.alvo === alvo ? carga.perfil : null;
  const inscricoes = carga.alvo === alvo ? carga.inscricoes : [];
  const erroCarregar = carga.alvo === alvo ? carga.erro : '';

  const [editando, setEditando] = useState(false);
  const [copiado, setCopiado] = useState(false);

  useEffect(() => {
    let ativo = true;
    // As inscrições são um complemento: se a consulta falhar, o portfólio aparece mesmo assim
    const minhas = meuPerfil ? listarMinhasCandidaturas().catch(() => []) : Promise.resolve([]);

    Promise.all([buscarPerfilPublico(alvo), minhas])
      .then(([dados, lista]) => {
        if (ativo) setCarga({ alvo, perfil: dados, inscricoes: lista, erro: '' });
      })
      .catch((err) => {
        if (!ativo) return;
        const mensagem = err.status === 404
          ? 'Este perfil não existe.'
          : 'Não consegui carregar o perfil. Confira se o back-end está rodando.';
        setCarga({ alvo, perfil: null, inscricoes: [], erro: mensagem });
      });

    return () => { ativo = false; };
  }, [alvo, meuPerfil]);

  async function compartilhar() {
    const endereco = `${window.location.origin}/student/profile/${perfil.id}`;
    try {
      await navigator.clipboard.writeText(endereco);
      setCopiado(true);
      setTimeout(() => setCopiado(false), 2500);
    } catch {
      window.prompt('Copie o link do perfil:', endereco);
    }
  }

  function aoSalvar(salvo) {
    setCarga((atual) => ({ ...atual, perfil: { ...atual.perfil, ...salvo } }));
    setEditando(false);
  }

  const trabalhos = perfil?.trabalhos ?? [];
  const numeros = resumo(trabalhos);
  const emAndamento = inscricoes.filter((i) => i.status !== 'AVALIADO');
  const github = linkSeguro(perfil?.github);
  const linkedin = linkSeguro(perfil?.linkedin);

  return (
    <div className="surface-paper">
      <header className="topbar">
        <div className="crumb">
          TRAMPOLIM / <b>PERFIL DO ALUNO</b>
        </div>

        <div className="top-actions">
          <span className="status grey-status">VISÍVEL SEM LOGIN</span>
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

      {erroCarregar && (
        <div className="empty-state" role="alert">
          <p>{erroCarregar}</p>
          <button type="button" className="btn btn-primary" onClick={() => navigate('/')}>Voltar para o início</button>
        </div>
      )}

      {!perfil && !erroCarregar && <div className="desafios-loading">Carregando perfil…</div>}

      {perfil && (
        <>
          <section className="profile-banner">
            <div className="avatar-large">{iniciais(perfil.name)}</div>
            <div className="profile-info">
              <h1>{perfil.name}</h1>
              <div className="label-orange">
                {perfil.professionalTitle || (perfil.type === 'MENTOR' ? 'MENTOR NO TRAMPOLIM' : 'ALUNO NO TRAMPOLIM')}
              </div>
              <p>
                {perfil.bio || (meuPerfil
                  ? 'Conte quem é você e o que está construindo: clique em "Editar perfil".'
                  : 'Esta pessoa ainda não escreveu uma apresentação.')}
              </p>

              <div className="profile-links">
                {github && (
                  <a href={github} target="_blank" rel="noreferrer" className="link-badge">
                    {semProtocolo(github)} ↗
                  </a>
                )}
                {linkedin && (
                  <a href={linkedin} target="_blank" rel="noreferrer" className="link-badge">
                    {semProtocolo(linkedin)} ↗
                  </a>
                )}
              </div>
            </div>

            <div className="profile-banner-actions">
              {meuPerfil && (
                <button type="button" className="btn btn-outline-light" onClick={() => setEditando(true)}>
                  Editar perfil
                </button>
              )}
              <button type="button" className="btn btn-outline-light" onClick={compartilhar}>
                {copiado ? 'Link copiado ✓' : 'Compartilhar perfil ↗'}
              </button>
            </div>
          </section>

          {editando && <EditarPerfil perfil={perfil} onSalvo={aoSalvar} onCancelar={() => setEditando(false)} />}

          <div className="profile-layout">
            <section>
              {meuPerfil && (
                <>
                  <div className="section-title-wrap">
                    <h2>Minhas inscrições</h2>
                  </div>

                  {emAndamento.length === 0 ? (
                    <p className="profile-vazio">Nenhuma inscrição em andamento. Que tal escolher um desafio?</p>
                  ) : (
                    <div className="validated-list">
                      {emAndamento.map((inscricao) => (
                        <article className="validated-card" key={inscricao.id}>
                          <div>
                            <h3>{inscricao.tituloDesafio}</h3>
                            <p>{LABEL_STATUS[inscricao.status] ?? inscricao.status}</p>
                          </div>
                          {inscricao.status !== 'REJEITADO' && (
                            <button
                              type="button"
                              className="text-button"
                              onClick={() => navigate(`/student/challenge/${inscricao.desafioId}/send-challenge`)}
                            >
                              {inscricao.status === 'ENVIADO' ? 'Ver entrega' : 'Enviar projeto'}
                            </button>
                          )}
                        </article>
                      ))}
                    </div>
                  )}
                </>
              )}

              <div className="section-title-wrap">
                <h2>Trabalho validado</h2>
                <button
                  type="button"
                  className="text-button"
                  onClick={() => navigate('/student/search')}
                >
                  Ver todos os desafios
                </button>
              </div>

              {trabalhos.length === 0 ? (
                <p className="profile-vazio">Os trabalhos avaliados por mentores vão aparecer aqui.</p>
              ) : (
                <div className="validated-list">
                  {trabalhos.map((item) => (
                    <article className="validated-card" key={item.candidaturaId}>
                      <div>
                        <h3>{item.tituloDesafio}</h3>
                        <p>
                          Projeto avaliado por {item.nomeMentor} ·{' '}
                          {item.feedback}
                        </p>

                        <div className="chip-group">
                          {listaDeTecnologias(item.tecnologias).map((tag) => (
                            <span key={tag} className="tag tag-chip-sm">{tag}</span>
                          ))}
                          <span className="tag tag-chip-sm tag-chip-evaluated">Avaliado</span>
                          {linkSeguro(item.linkEnvio) && (
                            <a className="tag tag-chip-sm" href={linkSeguro(item.linkEnvio)} target="_blank" rel="noreferrer">
                              Ver repositório ↗
                            </a>
                          )}
                        </div>
                      </div>

                      <div className="score-badge">{item.nota}</div>
                    </article>
                  ))}
                </div>
              )}
            </section>

            <aside className="profile-sidebar">
              <div className="metrics-grid">
                <div className="metric-box">
                  <b>{String(numeros.desafios).padStart(2, '0')}</b>
                  <span>DESAFIOS</span>
                </div>
                <div className="metric-box">
                  <b>{numeros.media}</b>
                  <span>MÉDIA</span>
                </div>
                <div className="metric-box">
                  <b>{String(numeros.tecnologias).padStart(2, '0')}</b>
                  <span>TECNOLOGIAS</span>
                </div>
              </div>

              <div className="activity-card">
                <h3>Ritmo de entregas</h3>
                <div className="activity-grid">
                  {ritmoDeEntregas(trabalhos).map((quantidade, index) => (
                    <div key={index} className={`activity-square level-${Math.min(quantidade, 3)}`} />
                  ))}
                </div>
                <div className="activity-footer">
                  <span>Últimos 6 meses</span>
                  <span>entregas concluídas</span>
                </div>
              </div>

              {trabalhos.length > 0 && (
                <div className="quote-card">
                  <p>“{trabalhos[0].feedback}”</p>
                  <span>{trabalhos[0].nomeMentor.toUpperCase()} · MENTOR</span>
                </div>
              )}
            </aside>
          </div>
        </>
      )}
    </div>
  );
}
