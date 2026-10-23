// IMPORTAÇÕES
import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router'
import Logo from '../../components/Logo/Logo'
import './Cadastro.css'

export default function Cadastro() {
  const navigate = useNavigate();
  const [perfil, setPerfil] = useState('Aluno');
  const [form, setForm] = useState({
    name: '',
    email: '',
    senha: '',
    type: perfil,
  })

  function refreshForm(e) {
    const { id, value } = e.target
    setForm(f => ({ ...f, [id]: value }))
  }

  useEffect(() => {
    setForm(f => ({ ...f, type: perfil}))
  }, [perfil])

  function whenSubmit(e) {
    e.preventDefault()
    console.log(form)
  }

  return (
    <div className="register-wrap">
      <div className="auth-visual">
        <Logo />

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
      </div>

      <div className="auth-form-side">
        <form className="auth-form">
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
            <label htmlFor="name">Nome</label>
            <input id="name" type="text" placeholder="Qual o seu nome?" onChange={refreshForm} required/>
          </div>

          <div className="field">
            <label htmlFor="email">E-mail</label>
            <input id="email" type="email" placeholder="Exemplo@email.com" onChange={refreshForm} required/>
          </div>

          <div className="field">
            <label htmlFor="password">Senha</label>
            <input id="senha" type="password" placeholder="Mínimo de 8 caracteres" onChange={refreshForm} required/>
          </div>

          <button className="btn btn-primary auth-submit" type="submit" onSubmit={whenSubmit}>
            Criar conta como {perfil.toLowerCase()}
          </button>
          
          <p className="form-footnote">
            Já participa?{' '}<button type="button" className="text-button" onClick={() => navigate('/login')}>Entrar na minha conta</button>
          </p>
        </form>
      </div>
    </div>
  )
}