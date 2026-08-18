import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function Login() {
  const navigate = useNavigate();
  const [perfil, setPerfil] = useState('Aluno');

  function handleSubmit(e) {
    e.preventDefault();
    // TODO(João Lucas): trocar por POST /api/auth/cadastro quando a API estiver pronta
    navigate(perfil === 'Aluno' ? '/desafios' : '/desafios/novo');
  }

  return (
    <div className="login-wrap">
      <div className="auth-visual">
        <span className="brand">
          <span className="brand-mark">T</span> Trampolim
        </span>
        <div className="auth-quote">
          <div className="micro-label">Seu primeiro passo</div>
          <h1>
            Faça do seu <span>trabalho</span> uma prova.
          </h1>
          <p>
            Entre na comunidade de pessoas que aprendem resolvendo problemas reais — e deixam
            evidências no caminho.
          </p>
        </div>
        <div className="auth-example">
          <p>
            "O feedback do desafio me ajudou a explicar meu projeto com muito mais segurança nas
            entrevistas."
          </p>
          <span className="who">— Bruna Lopes, desenvolvedora júnior</span>
        </div>
      </div>
      <div className="auth-form-side">
        <form className="auth-form" onSubmit={handleSubmit}>
          <h2>Crie sua conta</h2>
          <p>Escolha como você quer participar. Você poderá completar o perfil depois.</p>
          <div className="role-switch" role="group" aria-label="Tipo de perfil">
            <button
              type="button"
              className={perfil === 'Aluno' ? 'is-active' : undefined}
              onClick={() => setPerfil('Aluno')}
            >
              <strong>Sou aluno</strong>
              <small>Resolver e comprovar desafios</small>
            </button>
            <button
              type="button"
              className={perfil === 'Mentor' ? 'is-active' : undefined}
              onClick={() => setPerfil('Mentor')}
            >
              <strong>Sou mentor</strong>
              <small>Publicar e avaliar desafios</small>
            </button>
          </div>
          <div className="field">
            <label htmlFor="auth-name">Nome</label>
            <input id="auth-name" type="text" placeholder="Como você quer ser chamado?" />
          </div>
          <div className="field">
            <label htmlFor="auth-email">E-mail</label>
            <input id="auth-email" type="email" placeholder="voce@email.com" />
          </div>
          <div className="field">
            <label htmlFor="auth-password">Senha</label>
            <input id="auth-password" type="password" placeholder="Mínimo de 8 caracteres" />
          </div>
          <button className="btn btn-primary auth-submit" type="submit">
            Criar conta como {perfil.toLowerCase()} <span className="arrow">→</span>
          </button>
          <p className="form-footnote">
            Já participa?{' '}
            <button type="button" className="text-button">
              Entrar na minha conta
            </button>
          </p>
        </form>
      </div>
    </div>
  );
}
