import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import App from '../../App'
import { logarComo, mockApi, mockFetch } from '../../test/utils'

function renderTela() {
  return render(
    <MemoryRouter initialEntries={['/business/post-challenge']}>
      <App />
    </MemoryRouter>
  )
}

beforeEach(() => logarComo('MENTOR'))

afterEach(() => {
  cleanup()
  localStorage.clear()
  vi.useRealTimers()
  vi.unstubAllGlobals()
})

function renderEdicao(id = 3) {
  return render(
    <MemoryRouter initialEntries={[`/business/challenge/${id}/edit`]}>
      <App />
    </MemoryRouter>
  )
}

const EXISTENTE = {
  id: 3, titulo: 'API de pedidos', descricao: 'A loja precisa sair da planilha.', tecnologias: 'Java, Spring Boot',
  nivel: 'AVANCADO', prazo: '2030-12-31', vagas: 5, empresa: 'Loja Horizonte',
}

describe('PostarDesafio editando', () => {
  it('preenche o formulário com o desafio e salva com PUT', async () => {
    const fetch = mockApi({
      'GET /api/desafios/3': [200, EXISTENTE],
      'PUT /api/desafios/3': [200, { ...EXISTENTE, titulo: 'API de pedidos v2' }],
    })
    renderEdicao()

    expect(await screen.findByDisplayValue('API de pedidos')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Edite o desafio' })).toBeInTheDocument()
    expect(screen.getByLabelText('Prazo')).toHaveValue('2030-12-31')
    expect(screen.getByRole('button', { name: 'Remover Spring Boot' })).toBeInTheDocument()

    fireEvent.change(screen.getByLabelText('Título do desafio'), { target: { value: 'API de pedidos v2' } })
    fireEvent.click(screen.getByRole('button', { name: /Salvar alterações/ }))

    expect(await screen.findByText('Alterações salvas')).toBeInTheDocument()
    const put = fetch.mock.calls.find(([, opcoes]) => opcoes.method === 'PUT')
    expect(JSON.parse(put[1].body)).toEqual({
      titulo: 'API de pedidos v2', descricao: 'A loja precisa sair da planilha.', tecnologias: 'Java, Spring Boot',
      nivel: 'AVANCADO', prazo: '2030-12-31', vagas: 5, empresa: 'Loja Horizonte',
    })
  })

  it('explica quando outro mentor tenta salvar', async () => {
    mockApi({
      'GET /api/desafios/3': [200, EXISTENTE],
      'PUT /api/desafios/3': [403, { erro: 'Só o mentor que publicou o desafio pode alterá-lo.' }],
    })
    renderEdicao()

    await screen.findByDisplayValue('API de pedidos')
    fireEvent.click(screen.getByRole('button', { name: /Salvar alterações/ }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Só o mentor que publicou pode editar este desafio.')
  })

  it('avisa quando o desafio não existe', async () => {
    mockApi({ 'GET /api/desafios/3': [404, { erro: 'Desafio não encontrado.' }] })
    renderEdicao()

    expect(await screen.findByRole('alert')).toHaveTextContent('Não encontramos este desafio.')
  })
})

describe('PostarDesafio', () => {
  it('publica o desafio no formato da API e mostra a confirmação', async () => {
    // Fim de mês, tarde da noite: o prazo vira de mês e não pode andar um dia por causa do fuso
    vi.useFakeTimers({ toFake: ['Date'] })
    vi.setSystemTime(new Date(2026, 10, 25, 23, 30))
    logarComo('MENTOR')

    const fetch = mockFetch([201, {
      id: 3, titulo: 'API de pedidos', nivel: 'AVANCADO', tecnologias: 'Java, Spring Boot', prazo: '2026-12-09', vagas: 5, empresa: 'Loja Horizonte',
    }])
    renderTela()

    fireEvent.change(screen.getByLabelText('Título do desafio'), { target: { value: 'API de pedidos' } })
    fireEvent.change(screen.getByLabelText('Contexto e objetivo'), { target: { value: 'A loja precisa sair da planilha.' } })
    const tecnologia = screen.getByLabelText('Tecnologias')
    fireEvent.change(tecnologia, { target: { value: 'Java' } })
    fireEvent.keyDown(tecnologia, { key: 'Enter' })
    fireEvent.change(tecnologia, { target: { value: 'Spring Boot' } })
    fireEvent.keyDown(tecnologia, { key: 'Enter' })
    fireEvent.change(screen.getByLabelText('Nível'), { target: { value: 'AVANCADO' } })
    fireEvent.change(screen.getByLabelText('Prazo'), { target: { value: '14' } })
    fireEvent.change(screen.getByLabelText('Vagas'), { target: { value: '5' } })
    fireEvent.change(screen.getByLabelText('Em nome de uma empresa'), { target: { value: ' Loja Horizonte ' } })
    fireEvent.click(screen.getByRole('button', { name: /Publicar desafio/ }))

    expect(await screen.findByText('Desafio publicado')).toBeInTheDocument()
    expect(screen.getByText(/com prazo até 09\/12\/2026 e 5 vagas/)).toBeInTheDocument()
    expect(screen.getByText('Spring Boot')).toBeInTheDocument()
    const [url, opcoes] = fetch.mock.calls[0]
    expect(url).toMatch(/\/api\/desafios$/)
    expect(opcoes.headers.Authorization).toMatch(/^Bearer /)
    expect(JSON.parse(opcoes.body)).toEqual({
      titulo: 'API de pedidos',
      descricao: 'A loja precisa sair da planilha.',
      tecnologias: 'Java, Spring Boot',
      nivel: 'AVANCADO',
      prazo: '2026-12-09',
      vagas: 5,
      empresa: 'Loja Horizonte',
    })
  })

  it('remove a tecnologia ao clicar no chip', () => {
    renderTela()
    const tecnologia = screen.getByLabelText('Tecnologias')
    fireEvent.change(tecnologia, { target: { value: 'React' } })
    fireEvent.keyDown(tecnologia, { key: 'Enter' })

    fireEvent.click(screen.getByRole('button', { name: 'Remover React' }))

    expect(screen.queryByRole('button', { name: 'Remover React' })).not.toBeInTheDocument()
  })

  it('mostra os erros de validação vindos do back ao lado de cada campo', async () => {
    mockFetch([400, { titulo: 'O título é obrigatório', prazo: 'O prazo é obrigatório', tecnologias: 'As tecnologias são obrigatórias' }])
    renderTela()

    fireEvent.change(screen.getByLabelText('Título do desafio'), { target: { value: ' ' } })
    fireEvent.change(screen.getByLabelText('Contexto e objetivo'), { target: { value: 'Contexto' } })
    fireEvent.submit(screen.getByRole('button', { name: /Publicar desafio/ }).closest('form'))

    expect(await screen.findByText('O título é obrigatório')).toBeInTheDocument()
    expect(screen.getByText('O prazo é obrigatório')).toBeInTheDocument()
    expect(screen.getByText('As tecnologias são obrigatórias')).toBeInTheDocument()
    expect(screen.getByRole('alert')).toHaveTextContent('Confira os campos destacados.')
  })
})
