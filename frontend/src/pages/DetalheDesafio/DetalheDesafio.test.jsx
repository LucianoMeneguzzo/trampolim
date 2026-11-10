import { afterEach, describe, expect, it, vi } from 'vitest'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import App from '../../App'
import { logarComo, mockApi } from '../../test/utils'

const DESAFIO = {
  id: 7, titulo: 'API de pedidos', descricao: 'A loja precisa sair da planilha.', tecnologias: 'Java, Spring Boot',
  nivel: 'INTERMEDIARIO', prazo: '2030-12-31', vagas: 20, empresa: 'Loja Horizonte', dataCriacao: '2026-10-01T10:00:00',
  nomeCriador: 'Rafael Freitas', criadorId: 1, status: 'ABERTO', inscritos: 3,
}

function renderTela() {
  return render(
    <MemoryRouter initialEntries={['/student/challenge/7']}>
      <App />
    </MemoryRouter>
  )
}

afterEach(() => {
  cleanup()
  localStorage.clear()
  vi.unstubAllGlobals()
})

describe('Detalhe do desafio', () => {
  it('mostra os dados do desafio e leva o visitante para o login', async () => {
    mockApi({ 'GET /api/desafios/7': [200, DESAFIO] })
    renderTela()

    expect(await screen.findByRole('heading', { name: 'API de pedidos' })).toBeInTheDocument()
    expect(screen.getByText('A loja precisa sair da planilha.')).toBeInTheDocument()
    expect(screen.getByText('Spring Boot')).toBeInTheDocument()
    expect(screen.getByText('31/12/2030')).toBeInTheDocument()
    expect(screen.getByText('3 de 20 vagas')).toBeInTheDocument()
    expect(screen.getByText('Rafael Freitas')).toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: /Entrar para se inscrever/ }))

    expect(await screen.findByRole('heading', { name: 'Entre na sua conta' })).toBeInTheDocument()
    expect(screen.getByText('Faça login para continuar.')).toBeInTheDocument()
  })

  it('inscreve o aluno e abre a tela de envio', async () => {
    logarComo('STUDENT', 'Ana Martins')
    const inscricao = { id: 40, desafioId: 7, tituloDesafio: 'API de pedidos', status: 'PENDENTE', linkEnvio: null }
    const fetch = mockApi({
      'GET /api/desafios/7': [200, DESAFIO],
      'GET /api/candidaturas/minhas': [200, []],
      'POST /api/candidaturas': [201, inscricao],
    })
    renderTela()

    fireEvent.click(await screen.findByRole('button', { name: /Inscrever-me/ }))

    expect(await screen.findByRole('heading', { name: 'Envie seu projeto' })).toBeInTheDocument()
    const post = fetch.mock.calls.find(([, opcoes]) => opcoes.method === 'POST')
    expect(JSON.parse(post[1].body)).toEqual({ desafioId: 7 })
  })

  it('mostra o erro quando a inscrição é recusada', async () => {
    logarComo('STUDENT')
    mockApi({
      'GET /api/desafios/7': [200, DESAFIO],
      'GET /api/candidaturas/minhas': [200, []],
      'POST /api/candidaturas': [409, { erro: 'As vagas deste desafio acabaram.' }],
    })
    renderTela()

    fireEvent.click(await screen.findByRole('button', { name: /Inscrever-me/ }))

    expect(await screen.findByRole('alert')).toHaveTextContent('As vagas deste desafio acabaram.')
  })

  it.each([
    ['PENDENTE', /Enviar meu projeto/],
    ['ENVIADO', /Ver minha entrega/],
    ['AVALIADO', /Ver minha avaliação/],
  ])('aluno já inscrito (%s) vai direto para a entrega', async (status, botao) => {
    logarComo('STUDENT')
    mockApi({
      'GET /api/desafios/7': [200, DESAFIO],
      'GET /api/candidaturas/minhas': [200, [{ id: 40, desafioId: 7, status, tituloDesafio: 'API de pedidos' }]],
    })
    renderTela()

    expect(await screen.findByRole('button', { name: botao })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Inscrever-me/ })).not.toBeInTheDocument()
  })

  it('avisa quando a inscrição do aluno foi rejeitada', async () => {
    logarComo('STUDENT')
    mockApi({
      'GET /api/desafios/7': [200, DESAFIO],
      'GET /api/candidaturas/minhas': [200, [{ id: 40, desafioId: 7, status: 'REJEITADO' }]],
    })
    renderTela()

    expect(await screen.findByText('Sua inscrição neste desafio não foi aprovada.')).toBeInTheDocument()
  })

  it.each([
    [{ status: 'FECHADO' }, 'As inscrições deste desafio estão fechadas.'],
    [{ prazo: '2020-01-01' }, 'O prazo deste desafio já terminou.'],
    [{ vagas: 3 }, 'As vagas deste desafio acabaram.'],
  ])('não oferece inscrição quando %j', async (alteracao, mensagem) => {
    logarComo('STUDENT')
    mockApi({
      'GET /api/desafios/7': [200, { ...DESAFIO, ...alteracao }],
      'GET /api/candidaturas/minhas': [200, []],
    })
    renderTela()

    expect(await screen.findByText(mensagem)).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: /Inscrever-me/ })).not.toBeInTheDocument()
  })

  it('mentor dono gerencia pelo perfil; outro mentor só lê', async () => {
    logarComo('MENTOR')
    mockApi({ 'GET /api/desafios/7': [200, DESAFIO] })
    renderTela()
    expect(await screen.findByRole('button', { name: 'Gerenciar no meu perfil' })).toBeInTheDocument()
    cleanup()

    mockApi({ 'GET /api/desafios/7': [200, { ...DESAFIO, criadorId: 99 }] })
    renderTela()
    expect(await screen.findByText('As inscrições são para contas de aluno.')).toBeInTheDocument()
  })

  it('explica quando o desafio não existe', async () => {
    mockApi({ 'GET /api/desafios/7': [404, { erro: 'Desafio não encontrado.' }] })
    renderTela()

    expect(await screen.findByRole('alert')).toHaveTextContent('Este desafio não existe ou foi removido.')
  })

  it('o desafio aparece mesmo se a lista de inscrições falhar', async () => {
    logarComo('STUDENT')
    mockApi({
      'GET /api/desafios/7': [200, DESAFIO],
      'GET /api/candidaturas/minhas': [500, { erro: 'Falhou' }],
    })
    renderTela()

    expect(await screen.findByRole('heading', { name: 'API de pedidos' })).toBeInTheDocument()
  })
})
