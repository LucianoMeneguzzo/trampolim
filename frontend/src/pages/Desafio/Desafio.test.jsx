import { afterEach, describe, expect, it, vi } from 'vitest'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import App from '../../App'
import { logarComo, mockApi } from '../../test/utils'

const DESAFIOS = [
  { id: 1, titulo: 'API de pedidos', descricao: 'Uma loja sem planilha.', tecnologias: 'Java, Spring Boot', nivel: 'INTERMEDIARIO', empresa: 'Loja Horizonte', nomeCriador: 'Rafael', status: 'ABERTO' },
  { id: 2, titulo: 'Landing page', descricao: 'Uma cafeteria na internet.', tecnologias: 'HTML, CSS', nivel: 'INICIANTE', empresa: null, nomeCriador: 'Marina', status: 'ABERTO' },
  { id: 3, titulo: 'Desafio encerrado', descricao: 'Já acabou.', tecnologias: 'Go', nivel: 'AVANCADO', nomeCriador: 'Caio', status: 'FECHADO' },
]

function renderTela() {
  return render(
    <MemoryRouter initialEntries={['/student/search']}>
      <App />
    </MemoryRouter>
  )
}

afterEach(() => {
  cleanup()
  localStorage.clear()
  vi.unstubAllGlobals()
})

describe('Lista de desafios', () => {
  it('mostra os desafios da API, sem os fechados, e funciona sem login', async () => {
    mockApi({ 'GET /api/desafios': [200, DESAFIOS] })
    renderTela()

    expect(await screen.findByRole('heading', { name: 'API de pedidos' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Landing page' })).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: 'Desafio encerrado' })).not.toBeInTheDocument()
    expect(screen.getByText('02 resultados')).toBeInTheDocument()
    expect(screen.getByText('Loja Horizonte')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Entrar' })).toBeInTheDocument()
  })

  it('busca por tecnologia e filtra por nível', async () => {
    mockApi({ 'GET /api/desafios': [200, DESAFIOS] })
    renderTela()
    await screen.findByRole('heading', { name: 'API de pedidos' })

    fireEvent.change(screen.getByRole('searchbox'), { target: { value: 'spring' } })
    expect(screen.getByText('01 resultado')).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: 'Landing page' })).not.toBeInTheDocument()

    fireEvent.change(screen.getByRole('searchbox'), { target: { value: '' } })
    fireEvent.click(screen.getByRole('button', { name: 'Iniciante' }))
    expect(screen.getByRole('heading', { name: 'Landing page' })).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: 'API de pedidos' })).not.toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'Avançado' }))
    expect(screen.getByText('Nenhum desafio com esses filtros. Tente remover algum filtro.')).toBeInTheDocument()
  })

  it('abre o detalhe ao clicar no card', async () => {
    mockApi({
      'GET /api/desafios': [200, DESAFIOS],
      'GET /api/desafios/1': [200, { ...DESAFIOS[0], inscritos: 2, vagas: 5, prazo: '2030-12-31', dataCriacao: '2026-10-01T10:00:00', criadorId: 9 }],
    })
    renderTela()

    fireEvent.click(await screen.findByRole('heading', { name: 'API de pedidos' }))

    expect(await screen.findByText('Antes de começar')).toBeInTheDocument()
  })

  it('avisa quando não há desafios e quando a API está fora do ar', async () => {
    mockApi({ 'GET /api/desafios': [200, []] })
    renderTela()
    expect(await screen.findByText('Ainda não há desafios abertos. Volte em breve.')).toBeInTheDocument()
    cleanup()

    mockApi({ 'GET /api/desafios': [500, { erro: 'Falhou' }] })
    renderTela()
    expect(await screen.findByRole('alert')).toHaveTextContent('Não consegui carregar os desafios')
  })

  it('mostra o menu da conta para quem está logado e permite sair', async () => {
    logarComo('STUDENT', 'Ana Martins')
    mockApi({ 'GET /api/desafios': [200, []] })
    renderTela()

    expect(await screen.findByRole('button', { name: 'Meu perfil' })).toBeInTheDocument()
    expect(screen.getByText('AM')).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'Sair' }))

    expect(localStorage.getItem('token')).toBeNull()
  })
})
