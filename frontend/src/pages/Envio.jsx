// Responsável: Andrius
// Referência visual: telas.html, linhas ~910-951 (section id="send")
export default function Envio() {
  return (
    <div className="surface-paper">
      <header className="topbar">
        <div className="crumb">
        MINHAS ENTREGAS / <b>API DE PEDIDOS</b>
        </div>

        <div className="top-actions">
          <span className="status amber-status">Aguardando envio</span>
          <div className="avatar">AM</div>
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

        <div className="workflow" aria-label="Etapas da entrega">
          <div className="workflow-step done">
            Inscrição feita
          </div>

          <div className="workflow-step active">
            Envie o projeto
          </div>

          <div className="workflow-step">
            Avaliação do mentor
          </div>

          <div className="workflow-step">
            Portfólio validado
          </div>
        </div>

        <div className="send-layout">
          <form
            className="send-card"
            onSubmit={(event) => event.preventDefault()}
          >
            <h2>Link do repositório</h2>

            <p>
              Use um repositório público no GitHub. A validação confirma o
              projeto, a linguagem principal e seu último commit.
            </p>

            <div className="field">
              <label htmlFor="repo-url">
                URL do GitHub
              </label>

              <div className="repo-input">
                <span>⌘</span>

                <input
                  id="repo-url"
                  type="url"
                  defaultValue="github.com/ana-martins/api-pedidos"
                  aria-describedby="repo-valid"
                />
              </div>

              <div className="valid-message" id="repo-valid">
                <i>✓</i> Repositório encontrado e pronto para envio.
              </div>
            </div>

            <div
              className="repo-data"
              aria-label="Dados do repositório validados"
            >
              <div>
                <span>Linguagem principal</span>
                <strong>Java</strong>
              </div>

              <div>
                <span>Último commit</span>
                <strong>Hoje, 14:32</strong>
              </div>

              <div>
                <span>Visibilidade</span>
                <strong>Público</strong>
              </div>
            </div>

            <button
              type="button"
              className="btn btn-primary"
            >
              Enviar para avaliação <span className="arrow">→</span>
            </button>
          </form>

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
      </div>
    </div>
  );
}
