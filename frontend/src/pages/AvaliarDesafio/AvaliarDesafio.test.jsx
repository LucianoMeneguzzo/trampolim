import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import App from '../../App'
import { logarComo, mockFetch } from '../../test/utils'

const ENTREGA = {
  id: 12,
  status: 'ENVIADO',
  dataCandidatura: '2026-10-20T10:00:00',
  dataEnvio: '2026-10-26T14:32:00',
  linkRepositorio: 'https://github.com/ana-martins/api-pedidos',
  repoLinguagem: 'Java',
  repoUltimoCommit: null,
  candidatoId: 2,
  nomeCandidato: 'Ana Martins',
  desafioId: 3,
  tituloDesafio: 'API de pedidos',
  prazoDias: 7,
  avaliacao: null,
}

const FEEDBACK = 'A estrutura da API está clara e as respostas de erro estão bem pensadas.'

function renderTela(id = 12) {
  return render(
    <MemoryRouter initialEntries={[`/business/assessment/${id}`]}>
      <App />
    </MemoryRouter>
  )
}

beforeEach(() => logarComo('MENTOR'))

afterEach(() => {
  cleanup()
  localStorage.clear()
  vi.unstubAllGlobals()
})

describe('AvaliarDesafio', () => {
  it('carrega a entrega e publica nota + feedback', async () => {
    const fetch = mockFetch(
      [200, ENTREGA],
      [201, { id: 1, candidaturaId: 12, nota: 8.5, feedback: FEEDBACK, dataAvaliacao: '2026-10-27T09:00:00', nomeMentor: 'Rafael Freitas' }],
    )
    renderTela()

    expect(await screen.findByRole('heading', { name: 'Ana Martins' })).toBeInTheDocument()
    expect(screen.getByText('github.com/ana-martins/api-pedidos')).toBeInTheDocument()
    expect(fetch.mock.calls[0][0]).toMatch(/\/api\/candidaturas\/12$/)

    fireEvent.change(screen.getByLabelText('Nota atribuída'), { target: { value: '8.5' } })
    fireEvent.change(screen.getByLabelText('Feedback para Ana'), { target: { value: FEEDBACK } })
    fireEvent.click(screen.getByRole('button', { name: /Publicar avaliação para Ana/ }))

    expect(await screen.findByRole('heading', { name: 'Avaliação publicada' })).toBeInTheDocument()
    expect(fetch.mock.calls[1][0]).toMatch(/\/api\/candidaturas\/12\/avaliacao$/)
    expect(JSON.parse(fetch.mock.calls[1][1].body)).toEqual({ nota: 8.5, feedback: FEEDBACK })
  })

  it('não libera a avaliação enquanto o aluno não enviou o projeto', async () => {
    mockFetch([200, { ...ENTREGA, status: 'PENDENTE', linkRepositorio: null, dataEnvio: null }])
    renderTela()

    expect(await screen.findByText('Projeto ainda não enviado')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Publicar avaliação/ })).toBeDisabled()
  })

  it('mostra a avaliação já feita em modo leitura', async () => {
    mockFetch([200, {
      ...ENTREGA,
      status: 'AVALIADO',
      avaliacao: { id: 1, nota: 9, feedback: FEEDBACK, dataAvaliacao: '2026-10-27T09:00:00', nomeMentor: 'Rafael Freitas' },
    }])
    renderTela()

    expect(await screen.findByRole('heading', { name: 'Avaliação publicada' })).toBeInTheDocument()
    expect(screen.getByText('9.0')).toBeInTheDocument()
    expect(screen.queryByLabelText('Feedback para Ana')).not.toBeInTheDocument()
  })

  it('explica quando a entrega é de um desafio de outro mentor', async () => {
    mockFetch([403, { erro: 'Só o mentor que publicou o desafio pode acessar estas inscrições.' }])
    renderTela(99)

    expect(await screen.findByRole('alert')).toHaveTextContent('Esta entrega pertence a um desafio de outro mentor.')
  })
})
