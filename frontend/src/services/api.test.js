import { afterEach, describe, expect, it, vi } from 'vitest'
import { apiFetch, ApiError, getUser } from './api'
import { fakeToken, logarComo, mockFetch } from '../test/utils'

afterEach(() => {
  localStorage.clear()
  vi.unstubAllGlobals()
})

describe('getUser', () => {
  it('devolve o usuário quando o token ainda é válido', () => {
    logarComo('MENTOR', 'Rafael')
    expect(getUser()).toEqual({ userId: 1, name: 'Rafael', type: 'MENTOR' })
  })

  it('limpa a sessão quando o token expirou', () => {
    localStorage.setItem('token', fakeToken({ expEmSegundos: -10 }))
    localStorage.setItem('user', JSON.stringify({ userId: 1, name: 'Ana', type: 'STUDENT' }))

    expect(getUser()).toBeNull()
    expect(localStorage.getItem('token')).toBeNull()
  })

  it('devolve null sem sessão', () => {
    expect(getUser()).toBeNull()
  })
})

describe('apiFetch', () => {
  it('manda o token nas rotas protegidas, mas não no login', async () => {
    logarComo('MENTOR')
    const fetch = mockFetch([200, {}], [200, {}])

    await apiFetch('/api/usuarios/me')
    await apiFetch('/api/auth/login', { method: 'POST' })

    expect(fetch.mock.calls[0][1].headers.Authorization).toMatch(/^Bearer /)
    expect(fetch.mock.calls[1][1].headers.Authorization).toBeUndefined()
  })

  it('transforma a resposta de erro da API em ApiError com a mensagem do back', async () => {
    mockFetch([409, { erro: 'Este e-mail já está cadastrado.' }])

    const erro = await apiFetch('/api/auth/cadastro', { method: 'POST' }).catch(e => e)

    expect(erro).toBeInstanceOf(ApiError)
    expect(erro.status).toBe(409)
    expect(erro.message).toBe('Este e-mail já está cadastrado.')
  })

  it('expõe os erros de validação por campo', async () => {
    mockFetch([400, { password: 'A senha precisa ter entre 8 e 72 caracteres' }])

    const erro = await apiFetch('/api/auth/cadastro', { method: 'POST' }).catch(e => e)

    expect(erro.campos.password).toBeDefined()
    expect(erro.message).toBe('A senha precisa ter entre 8 e 72 caracteres')
  })

  it('encerra a sessão local quando o back responde 401', async () => {
    logarComo('MENTOR')
    mockFetch([401, { erro: 'Faça login para continuar.' }])

    await apiFetch('/api/usuarios/me').catch(() => {})

    expect(localStorage.getItem('token')).toBeNull()
  })
})
