// IMPORTAÇÕES
import { useState } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router'
import Logo from '../../components/Logo/Logo'
import { getUser } from '../../services/api'
import { entrar, destinoInicial, mensagemDeErro } from '../../services/auth'
import '../Cadastro/Cadastro.css' // mesmo layout da tela de cadastro (auth-visual + auth-form)
import './Login.css'

export default function Login() {
  const navigate = useNavigate()
  const location = useLocation()
  const [form, setForm] = useState({ email: '', password: '' })
  const [erro, setErro] = useState('')
  const [enviando, setEnviando] = useState(false)

  // Se a pessoa foi barrada numa rota protegida, o ProtectedRoute manda de onde ela veio
  const voltarPara = location.state?.from

  function refreshForm(e) {
    const { id, value } = e.target
    setForm(f => ({ ...f, [id]: value }))
  }

  async function whenSubmit(e) {
    e.preventDefault()
    setErro('')
    setEnviando(true)

    try {
      // POST /api/auth/login -> guarda token + { userId, name, type } no localStorage
      const dados = await entrar({ email: form.email.trim(), senha: form.password })
      navigate(voltarPara ?? destinoInicial(dados.type), { replace: true })
    } catch (err) {
      setErro(mensagemDeErro(err))
      setEnviando(false)
    }
  }

  // Quem já está logado vai direto para a própria área
  const usuario = getUser()
  if (usuario) return <Navigate to={destinoInicial(usuario.type)} replace />

  return (
    <div className="register-wrap">
      <div className="auth-visual">
        <Logo />

        <div className="auth-quote">
          <div className="micro-label">Bem-vindo de volta</div>
          <h1>
            Continue construindo sua <span>prova</span>.
          </h1>
          <p>
            Entre para acompanhar seus desafios, enviar projetos e ver as avaliações que viraram
            portfólio.
          </p>
        </div>
      </div>

      <div className="auth-form-side">
        <form className="auth-form" onSubmit={whenSubmit}>
          <h2>Entre na sua conta</h2>
          <p>Use o e-mail e a senha que você cadastrou. Alunos e mentores entram pelo mesmo lugar.</p>

          {voltarPara && <p className="login-notice">Faça login para continuar.</p>}

          <div className="field">
            <label htmlFor="email">E-mail</label>
            <input id="email" type="email" placeholder="Exemplo@email.com" autoComplete="email"
              value={form.email} onChange={refreshForm} required />
          </div>

          <div className="field">
            <label htmlFor="password">Senha</label>
            <input id="password" type="password" placeholder="Sua senha" autoComplete="current-password"
              value={form.password} onChange={refreshForm} required />
          </div>

          {erro && <p className="auth-error" role="alert">{erro}</p>}

          <button className="btn btn-primary auth-submit" type="submit" disabled={enviando}>
            {enviando ? 'Entrando…' : 'Entrar'}
          </button>

          <p className="form-footnote">
            Ainda não participa?{' '}<button type="button" className="text-button" onClick={() => navigate('/register')}>Criar minha conta</button>
          </p>
        </form>
      </div>
    </div>
  )
}
