import { apiFetch, setSession, clearSession } from './api'

// O front usa "Aluno"/"Mentor"; a API espera STUDENT/MENTOR.
export const TIPO_NA_API = { Aluno: 'STUDENT', Mentor: 'MENTOR' }

// Tela inicial de cada perfil depois de entrar: o perfil da pessoa
const DESTINO_POR_TIPO = { STUDENT: '/student/profile', MENTOR: '/business/profile' }

export function destinoInicial(type) {
  return DESTINO_POR_TIPO[type] ?? '/'
}

// POST /api/auth/cadastro
export function cadastrar({ nome, email, senha, perfil }) {
  return apiFetch('/api/auth/cadastro', {
    method: 'POST',
    body: JSON.stringify({
      name: nome,
      email,
      password: senha,
      type: TIPO_NA_API[perfil],
    }),
  })
}

// POST /api/auth/login  (guarda token, userId, name e type no navegador)
export async function entrar({ email, senha }) {
  const dados = await apiFetch('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password: senha }),
  })
  setSession(dados)
  return dados
}

export function sair() {
  clearSession()
}

// GET /api/usuarios/me
export function buscarMeusDados() {
  return apiFetch('/api/usuarios/me')
}

// Texto amigável para os erros de login/cadastro
export function mensagemDeErro(err) {
  if (err instanceof TypeError) {
    return 'Não consegui falar com o servidor. Confira se o back-end está rodando.'
  }
  if (err.status === 401) return 'E-mail ou senha inválidos.'
  if (err.status === 409) return 'Esse e-mail já está cadastrado. Tente entrar na sua conta.'
  if (err.status === 400) return err.message
  return 'Algo deu errado. Tente de novo em instantes.'
}
