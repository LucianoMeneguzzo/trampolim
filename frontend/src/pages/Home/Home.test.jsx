import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
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

const METRICAS = { desafios: 3, inscricoes: 5, enviados: 2, avaliados: 1, taxaConclusao: 20, inscricoesPorMes: [] }

// Com chaves: se o beforeEach devolvesse a função do mock, o Vitest a chamaria ao fim de cada teste
beforeEach(() => {
  mockApi({ 'GET /api/metricas': [200, METRICAS] })
})

afterEach(() => {
  cleanup()
  localStorage.clear()
  vi.unstubAllGlobals()
})

describe('Home', () => {
  it('mostra os números reais da plataforma', async () => {
    renderTela()

    expect(await screen.findByText('3 desafios publicados · 1 entrega avaliada')).toBeInTheDocument()
  })

  it('fica só com a ilustração quando a API não responde', async () => {
    mockApi({ 'GET /api/metricas': [500, {}] })
    renderTela()

    expect(await screen.findByText('Exemplo de atividade')).toBeInTheDocument()
    expect(screen.queryByText(/desafios publicados/)).not.toBeInTheDocument()
  })

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
