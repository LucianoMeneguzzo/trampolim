// Camada única de comunicação com o back-end.
//   VITE_API_URL não definido -> http://localhost:8080
//   VITE_API_URL=https://...   -> back-end publicado
const API_URL = (import.meta.env.VITE_API_URL ?? 'http://localhost:8080').replace(/\/$/, '')

const TOKEN_KEY = 'token'
const USER_KEY = 'user'

// Erro de resposta da API. "message" já vem pronta para mostrar na tela;
// "campos" traz os erros de validação por campo ({ password: '...' }), quando houver.
export class ApiError extends Error {
  constructor(status, data) {
    const campos = data && typeof data === 'object' && !data.erro ? data : {}
    super(data?.erro ?? Object.values(campos)[0] ?? `Erro ${status}`)
    this.status = status
    this.campos = campos
  }
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setSession({ token, userId, name, type }) {
  localStorage.setItem(TOKEN_KEY, token)
  localStorage.setItem(USER_KEY, JSON.stringify({ userId, name, type }))
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

// O JWT é "cabeçalho.payload.assinatura" em base64url. O payload tem "exp" (em segundos).
// Ler o payload no front é só para UX; quem garante a segurança é a validação no back.
function tokenExpirado(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')))
    return !payload.exp || payload.exp * 1000 <= Date.now()
  } catch {
    return true
  }
}

// Usuário logado ({ userId, name, type }) ou null se não há sessão ou o token expirou
export function getUser() {
  const token = getToken()
  const raw = localStorage.getItem(USER_KEY)
  if (!token || !raw || tokenExpirado(token)) {
    clearSession()
    return null
  }
  try {
    return JSON.parse(raw)
  } catch {
    clearSession()
    return null
  }
}

export async function apiFetch(path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) }

  // Nas rotas de login/cadastro NÃO se manda o token: um token velho ou
  // expirado guardado no navegador não deve atrapalhar quem só quer entrar.
  const isAuthRoute = path.startsWith('/api/auth/')
  const token = getToken()
  if (token && !isAuthRoute) {
    headers.Authorization = `Bearer ${token}`
  }

  // Se o servidor estiver fora do ar, o fetch lança TypeError (tratado nas telas)
  const response = await fetch(`${API_URL}${path}`, { ...options, headers })

  const text = await response.text()
  let data = null
  if (text) {
    try {
      data = JSON.parse(text)
    } catch {
      data = null
    }
  }

  if (!response.ok) {
    // Token recusado pelo back (expirou ou foi invalidado): encerra a sessão local
    if (response.status === 401 && !isAuthRoute) clearSession()
    throw new ApiError(response.status, data)
  }

  return data
}
