// IMPORTAÇÕES
import { useState } from 'react'
import { Navigate, useNavigate } from 'react-router'
import Logo from '../../components/Logo/Logo'
import { getUser } from '../../services/api'
import { cadastrar, entrar, destinoInicial, mensagemDeErro } from '../../services/auth'
import './Cadastro.css'

export default function Cadastro() {
  const navigate = useNavigate();
  const [perfil, setPerfil] = useState('Aluno');
  const [form, setForm] = useState({
    name: '',
    email: '',
    password: '',
  })
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  function refreshForm(e) {
    const { id, value } = e.target
    setForm(f => ({ ...f, [id]: value }))
  }

  // FUNÇÃO PARA ENVIO DO FORMULÁRIO (CRIAÇÃO DE CONTA)
  async function whenSubmit(e) {
    e.preventDefault()
    setErro('')
    setEnviando(true)

    const email = form.email.trim()

    // 1) cria a conta (POST /api/auth/cadastro grava o usuário com a senha em hash)
    try {
      await cadastrar({ nome: form.name.trim(), email, senha: form.password, perfil })
    } catch (err) {
      setErro(mensagemDeErro(err))
      setEnviando(false)
      return
    }

    // 2) entra automaticamente (POST /api/auth/login guarda o token) e leva para a área do perfil
    try {
      const dados = await entrar({ email, senha: form.password })
      navigate(destinoInicial(dados.type), { replace: true })
    } catch {
      navigate('/login') // conta criada, mas o login automático falhou: a pessoa entra na mão
    }
  }

  // Quem já está logado não precisa criar conta de novo
  const usuario = getUser()
  if (usuario) return <Navigate to={destinoInicial(usuario.type)} replace />

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
        <form className="auth-form" onSubmit={whenSubmit}>
          <h2>Crie sua conta</h2>
          <p>Escolha como você quer participar. Você poderá completar o perfil depois.</p>

          <div className="role-switch" role="group" aria-label="Tipo de perfil">
            <button
              type="button"
              className={perfil === 'Aluno' ? 'is-active' : undefined}
              aria-pressed={perfil === 'Aluno'}
              onClick={() => setPerfil('Aluno')}
            >
              <strong>Sou aluno</strong>
              <small>Resolver e comprovar desafios</small>
            </button>

            <button
              type="button"
              className={perfil === 'Mentor' ? 'is-active' : undefined}
              aria-pressed={perfil === 'Mentor'}
              onClick={() => setPerfil('Mentor')}
            >
              <strong>Sou mentor</strong>
              <small>Publicar e avaliar desafios</small>
            </button>
          </div>

          <div className="field">
            <label htmlFor="name">Nome</label>
            <input id="name" type="text" placeholder="Qual o seu nome?" autoComplete="name" maxLength={100}
              value={form.name} onChange={refreshForm} required/>
          </div>

          <div className="field">
            <label htmlFor="email">E-mail</label>
            <input id="email" type="email" placeholder="Exemplo@email.com" autoComplete="email" maxLength={150}
              value={form.email} onChange={refreshForm} required/>
          </div>

          <div className="field">
            <label htmlFor="password">Senha</label>
            <input id="password" type="password" placeholder="Mínimo de 8 caracteres" autoComplete="new-password"
              minLength={8} maxLength={72} value={form.password} onChange={refreshForm} required/>
          </div>

          {erro && <p className="auth-error" role="alert">{erro}</p>}

          <button className="btn btn-primary auth-submit" type="submit" disabled={enviando}>
            {enviando ? 'Criando conta…' : `Criar conta como ${perfil.toLowerCase()}`}
          </button>

          <p className="form-footnote">
            Já participa?{' '}<button type="button" className="text-button" onClick={() => navigate('/login')}>Entrar na minha conta</button>
          </p>
        </form>
      </div>
    </div>
  )
}
