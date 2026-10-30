import { afterEach, describe, expect, it, vi } from 'vitest'
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, useLocation } from 'react-router'
import App from '../../App'
import { fakeToken, mockFetch } from '../../test/utils'

// Mostra o endereço atual, para o teste saber para qual tela o app foi
function RotaAtual() {
  return <output data-testid="rota">{useLocation().pathname}</output>
}

function renderEm(rota) {
  return render(
    <MemoryRouter initialEntries={[rota]}>
      <App />
      <RotaAtual />
    </MemoryRouter>
  )
}

function preencher(label, valor) {
  fireEvent.change(screen.getByLabelText(label), { target: { value: valor } })
}

afterEach(() => {
  cleanup()
  localStorage.clear()
  vi.unstubAllGlobals()
})

describe('Login', () => {
  it('guarda o token e leva o mentor para o perfil dele', async () => {
    const fetch = mockFetch([200, { userId: 7, name: 'Rafael', type: 'MENTOR', token: fakeToken() }])
    renderEm('/login')

    preencher('E-mail', 'rafael@trampolim.dev')
    preencher('Senha', 'senha-forte-123')
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }))

    await waitFor(() => expect(screen.getByTestId('rota')).toHaveTextContent('/business/profile'))
    expect(JSON.parse(fetch.mock.calls[0][1].body)).toEqual({ email: 'rafael@trampolim.dev', password: 'senha-forte-123' })
    expect(JSON.parse(localStorage.getItem('user'))).toEqual({ userId: 7, name: 'Rafael', type: 'MENTOR' })
  })

  it('mostra a mensagem quando a senha está errada', async () => {
    mockFetch([401, { erro: 'E-mail ou senha inválidos.' }])
    renderEm('/login')

    preencher('E-mail', 'rafael@trampolim.dev')
    preencher('Senha', 'errada123')
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('E-mail ou senha inválidos.')
    expect(localStorage.getItem('token')).toBeNull()
  })
})

describe('Cadastro', () => {
  it('cria a conta como mentor, entra automaticamente e abre o perfil', async () => {
    const fetch = mockFetch(
      [201, { id: 7, name: 'Rafael', email: 'rafael@trampolim.dev', type: 'MENTOR' }],
      [200, { userId: 7, name: 'Rafael', type: 'MENTOR', token: fakeToken() }],
    )
    renderEm('/register')

    fireEvent.click(screen.getByRole('button', { name: /Sou mentor/ }))
    preencher('Nome', 'Rafael')
    preencher('E-mail', 'rafael@trampolim.dev')
    preencher('Senha', 'senha-forte-123')
    fireEvent.click(screen.getByRole('button', { name: 'Criar conta como mentor' }))

    await waitFor(() => expect(screen.getByTestId('rota')).toHaveTextContent('/business/profile'))
    expect(fetch.mock.calls[0][0]).toMatch(/\/api\/auth\/cadastro$/)
    expect(JSON.parse(fetch.mock.calls[0][1].body)).toEqual({
      name: 'Rafael', email: 'rafael@trampolim.dev', password: 'senha-forte-123', type: 'MENTOR',
    })
  })

  it('avisa quando o e-mail já existe', async () => {
    mockFetch([409, { erro: 'Este e-mail já está cadastrado.' }])
    renderEm('/register')

    preencher('Nome', 'Ana')
    preencher('E-mail', 'ana@trampolim.dev')
    preencher('Senha', 'senha-forte-123')
    fireEvent.click(screen.getByRole('button', { name: 'Criar conta como aluno' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Esse e-mail já está cadastrado')
  })
})
