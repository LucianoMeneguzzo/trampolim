import { vi } from 'vitest'

// Gera um JWT "de mentira" (assinatura falsa) só com o payload que o front lê: exp em segundos
export function fakeToken({ expEmSegundos = 3600 } = {}) {
  const base64url = obj => btoa(JSON.stringify(obj)).replace(/=+$/, '').replace(/\+/g, '-').replace(/\//g, '_')
  const exp = Math.floor(Date.now() / 1000) + expEmSegundos
  return `${base64url({ alg: 'HS256' })}.${base64url({ sub: 'teste@trampolim.dev', exp })}.assinatura`
}

export function logarComo(type, name = 'Rafael Freitas') {
  localStorage.setItem('token', fakeToken())
  localStorage.setItem('user', JSON.stringify({ userId: 1, name, type }))
}

// Resposta no formato do fetch, com o corpo JSON informado
export function respostaJson(status, corpo) {
  return Promise.resolve({
    ok: status >= 200 && status < 300,
    status,
    text: () => Promise.resolve(corpo === undefined ? '' : JSON.stringify(corpo)),
  })
}

export function mockFetch(...respostas) {
  const fn = vi.fn()
  respostas.forEach(([status, corpo]) => fn.mockImplementationOnce(() => respostaJson(status, corpo)))
  vi.stubGlobal('fetch', fn)
  return fn
}
