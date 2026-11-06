import { afterEach, describe, expect, it, vi } from 'vitest'
import { cleanup, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import App from '../../App'
import { mockApi } from '../../test/utils'

const METRICAS = {
  desafios: 12,
  inscricoes: 40,
  enviados: 30,
  avaliados: 20,
  taxaConclusao: 50,
  inscricoesPorMes: [
    { mes: '2026-06', total: 2 }, { mes: '2026-07', total: 4 }, { mes: '2026-08', total: 0 },
    { mes: '2026-09', total: 8 }, { mes: '2026-10', total: 16 }, { mes: '2026-11', total: 10 },
  ],
}

function renderTela() {
  return render(
    <MemoryRouter initialEntries={['/metricas']}>
      <App />
    </MemoryRouter>
  )
}

afterEach(() => {
  cleanup()
  localStorage.clear()
  vi.unstubAllGlobals()
})

describe('Painel de métricas', () => {
  it('abre sem login e mostra os números da API', async () => {
    mockApi({ 'GET /api/metricas': [200, METRICAS] })
    renderTela()

    expect(await screen.findByText('Taxa de conclusão')).toBeInTheDocument()
    expect(screen.getByText('50%')).toBeInTheDocument()
    expect(screen.getByText('12')).toBeInTheDocument()
    expect(screen.getByText('NOV')).toBeInTheDocument()
    expect(screen.getByText('JUN')).toBeInTheDocument()
    expect(screen.getByRole('img')).toHaveAccessibleName(expect.stringContaining('OUT: 16'))
    expect(screen.getByText('20 de 40 inscrições já viraram trabalho avaliado por um mentor.')).toBeInTheDocument()
  })

  it('funciona com a plataforma ainda vazia', async () => {
    mockApi({
      'GET /api/metricas': [200, { ...METRICAS, desafios: 0, inscricoes: 0, enviados: 0, avaliados: 0, taxaConclusao: 0, inscricoesPorMes: METRICAS.inscricoesPorMes.map(m => ({ ...m, total: 0 })) }],
    })
    renderTela()

    expect(await screen.findByText('0 de 0 inscrições já viraram trabalho avaliado por um mentor.')).toBeInTheDocument()
  })

  it('avisa quando a API está fora do ar', async () => {
    mockApi({ 'GET /api/metricas': [500, {}] })
    renderTela()

    expect(await screen.findByRole('alert')).toHaveTextContent('Não consegui carregar as métricas')
  })
})
