import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import App from '../../App'
import { logarComo, mockFetch } from '../../test/utils'

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
  vi.unstubAllGlobals()
})

describe('PostarDesafio', () => {
  it('publica o desafio com as tecnologias adicionadas e mostra a confirmação', async () => {
    const fetch = mockFetch([201, {
      id: 3, titulo: 'API de pedidos', nivel: 'AVANCADO', tecnologias: ['Java', 'Spring Boot'], prazoDias: 14, vagas: 5,
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
    fireEvent.click(screen.getByRole('button', { name: /Publicar desafio/ }))

    expect(await screen.findByText('Desafio publicado')).toBeInTheDocument()
    const [url, opcoes] = fetch.mock.calls[0]
    expect(url).toMatch(/\/api\/desafios$/)
    expect(opcoes.headers.Authorization).toMatch(/^Bearer /)
    expect(JSON.parse(opcoes.body)).toEqual({
      title: 'API de pedidos',
      description: 'A loja precisa sair da planilha.',
      level: 'AVANCADO',
      technologies: ['Java', 'Spring Boot'],
      deadlineDays: 14,
      slots: 5,
      companyName: null,
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
    mockFetch([400, { title: 'O título é obrigatório' }])
    renderTela()

    fireEvent.change(screen.getByLabelText('Título do desafio'), { target: { value: ' ' } })
    fireEvent.change(screen.getByLabelText('Contexto e objetivo'), { target: { value: 'Contexto' } })
    fireEvent.submit(screen.getByRole('button', { name: /Publicar desafio/ }).closest('form'))

    expect(await screen.findByText('O título é obrigatório')).toBeInTheDocument()
    expect(screen.getByRole('alert')).toHaveTextContent('Confira os campos destacados.')
  })
})
