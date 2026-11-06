import { afterEach, describe, expect, it, vi } from 'vitest'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import App from '../../App'
import { logarComo, mockApi } from '../../test/utils'

function renderTela() {
  return render(
    <MemoryRouter initialEntries={['/']}>
      <App />
    </MemoryRouter>
  )
}

afterEach(() => {
  cleanup()
  localStorage.clear()
  vi.unstubAllGlobals()
})

describe('Home', () => {
  it('oferece entrar e criar perfil para visitantes', () => {
    renderTela()

    expect(screen.getByRole('button', { name: 'Entrar' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Criar perfil' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Meu perfil' })).not.toBeInTheDocument()
  })

  it('oferece o perfil para quem já entrou', () => {
    logarComo('STUDENT')
    renderTela()

    expect(screen.getByRole('button', { name: 'Meu perfil' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Entrar' })).not.toBeInTheDocument()
  })

  it('leva para a lista de desafios e para as métricas', async () => {
    mockApi({
      'GET /api/desafios': [200, []],
      'GET /api/metricas': [200, { desafios: 0, inscricoes: 0, enviados: 0, avaliados: 0, taxaConclusao: 0, inscricoesPorMes: [] }],
    })
    renderTela()

    fireEvent.click(screen.getByRole('button', { name: 'Métricas' }))
    expect(await screen.findByRole('heading', { name: 'Movimento da comunidade' })).toBeInTheDocument()
    cleanup()

    renderTela()
    fireEvent.click(screen.getByRole('button', { name: 'Explorar desafios' }))
    expect(await screen.findByRole('heading', { name: 'Encontre seu próximo desafio' })).toBeInTheDocument()
  })
})
