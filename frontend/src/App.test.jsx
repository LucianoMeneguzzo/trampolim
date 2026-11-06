import { afterEach, describe, expect, it, vi } from 'vitest'
import { cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter, useLocation } from 'react-router'
import App from './App'
import { logarComo } from './test/utils'

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

afterEach(() => {
  cleanup()
  localStorage.clear()
  vi.unstubAllGlobals()
})

describe('rotas protegidas', () => {
  it('manda para o login quem tenta abrir /business/post-challenge sem estar logado', () => {
    renderEm('/business/post-challenge')

    expect(screen.getByRole('heading', { name: 'Entre na sua conta' })).toBeInTheDocument()
    expect(screen.getByText('Faça login para continuar.')).toBeInTheDocument()
  })

  it('abre /business/post-challenge para mentor', () => {
    logarComo('MENTOR')
    renderEm('/business/post-challenge')

    expect(screen.getByRole('heading', { name: 'Publique um desafio' })).toBeInTheDocument()
  })

  it('aluno que tenta abrir a área do mentor volta para o próprio perfil', () => {
    logarComo('STUDENT', 'Ana Martins')
    renderEm('/business/assessment/1')

    expect(screen.getByTestId('rota')).toHaveTextContent('/student/profile')
  })

  it('quem já está logado e abre /login vai direto para o próprio perfil', () => {
    logarComo('MENTOR')
    renderEm('/login')

    expect(screen.getByTestId('rota')).toHaveTextContent('/business/profile')
  })
})
