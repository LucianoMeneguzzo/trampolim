import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { cleanup, fireEvent, render, screen, within } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import App from '../../App'
import { logarComo, mockApi } from '../../test/utils'

const EU = { id: 1, name: 'Rafael Freitas', email: 'rafael@trampolim.dev', type: 'MENTOR', bio: 'Backend há 8 anos.', professionalTitle: 'Arquiteto', linkedin: 'https://linkedin.com/in/rafael' }

const DESAFIOS = [
  { id: 3, titulo: 'API de pedidos', descricao: 'Sair da planilha.', tecnologias: 'Java, Spring Boot', prazo: '2030-12-31', status: 'ABERTO', inscritos: 2, entregas: 1 },
  { id: 4, titulo: 'Sem ninguém', descricao: 'Ainda vazio.', tecnologias: 'React', prazo: '2030-12-31', status: 'ABERTO', inscritos: 0, entregas: 0 },
]

function renderTela() {
  return render(
    <MemoryRouter initialEntries={['/business/profile']}>
      <App />
    </MemoryRouter>
  )
}

function card(titulo) {
  return screen.getByRole('heading', { name: titulo }).closest('article')
}

beforeEach(() => logarComo('MENTOR', 'Rafael Freitas'))

afterEach(() => {
  cleanup()
  localStorage.clear()
  vi.restoreAllMocks()
  vi.unstubAllGlobals()
})

describe('Perfil do mentor', () => {
  it('mostra os dados reais do mentor e os números dos desafios', async () => {
    mockApi({ 'GET /api/usuarios/me': [200, EU], 'GET /api/desafios/meus': [200, DESAFIOS] })
    renderTela()

    expect(await screen.findByRole('heading', { name: 'Rafael Freitas' })).toBeInTheDocument()
    expect(screen.getByText('Backend há 8 anos.')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /linkedin\.com\/in\/rafael/ })).toBeInTheDocument()
    expect(within(card('API de pedidos')).getByText('50% de entrega')).toBeInTheDocument()
    expect(within(card('API de pedidos')).getByText('Em andamento')).toBeInTheDocument()
    expect(within(card('Sem ninguém')).getByText('Aberto')).toBeInTheDocument()
    expect(screen.queryByText('Nexa Tecnologia')).not.toBeInTheDocument()
  })

  it('lista as inscrições e leva para a avaliação de quem já enviou', async () => {
    mockApi({
      'GET /api/usuarios/me': [200, EU],
      'GET /api/desafios/meus': [200, DESAFIOS],
      'GET /api/candidaturas/desafio/3': [200, [
        { id: 40, candidatoId: 5, nomeCandidato: 'Ana Martins', status: 'ENVIADO', linkEnvio: 'https://github.com/ana/api' },
        { id: 41, candidatoId: 6, nomeCandidato: 'Bruno Lima', status: 'PENDENTE', linkEnvio: null },
      ]],
      'GET /api/candidaturas/40': [200, {
        id: 40, status: 'ENVIADO', nomeCandidato: 'Ana Martins', tituloDesafio: 'API de pedidos', linkEnvio: 'https://github.com/ana/api',
        dataCandidatura: '2026-10-20T10:00:00', dataEnvio: '2026-10-21T10:00:00', avaliacao: null,
      }],
    })
    renderTela()
    await screen.findByRole('heading', { name: 'API de pedidos' })

    fireEvent.click(within(card('API de pedidos')).getByRole('button', { name: 'Ver inscrições' }))

    expect(await screen.findByRole('button', { name: 'Ana Martins' })).toBeInTheDocument()
    expect(screen.getByText('Aguardando envio')).toBeInTheDocument()
    expect(within(card('API de pedidos')).getAllByRole('button', { name: 'Avaliar' })).toHaveLength(1)

    fireEvent.click(within(card('API de pedidos')).getByRole('button', { name: 'Avaliar' }))

    expect(await screen.findByRole('heading', { name: 'Avalie a entrega' })).toBeInTheDocument()
  })

  it('aprova e recusa inscrições que ainda não têm envio', async () => {
    const fetch = mockApi({
      'GET /api/usuarios/me': [200, EU],
      'GET /api/desafios/meus': [200, DESAFIOS],
      'GET /api/candidaturas/desafio/3': [200, [
        { id: 40, candidatoId: 5, nomeCandidato: 'Ana Martins', status: 'ENVIADO', linkEnvio: 'https://github.com/ana/api' },
        { id: 41, candidatoId: 6, nomeCandidato: 'Bruno Lima', status: 'PENDENTE', linkEnvio: null },
      ]],
      'PATCH /api/candidaturas/41/status': [200, {}],
    })
    renderTela()
    await screen.findByRole('heading', { name: 'API de pedidos' })
    fireEvent.click(within(card('API de pedidos')).getByRole('button', { name: 'Ver inscrições' }))

    // Quem já enviou não pode mais ser recusado
    await screen.findByRole('button', { name: 'Bruno Lima' })
    expect(screen.queryByRole('button', { name: 'Recusar inscrição de Ana Martins' })).not.toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'Recusar inscrição de Bruno Lima' }))

    expect(await screen.findByText('Não aprovada')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Recusar inscrição de Bruno Lima' })).not.toBeInTheDocument()
    const patch = fetch.mock.calls.find(([, opcoes]) => opcoes.method === 'PATCH')
    expect(JSON.parse(patch[1].body)).toEqual({ status: 'REJEITADO' })

    fireEvent.click(screen.getByRole('button', { name: 'Aprovar inscrição de Bruno Lima' }))

    expect(await screen.findByText('Aprovada · aguardando envio')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Aprovar inscrição de Bruno Lima' })).not.toBeInTheDocument()
  })

  it('esconde a lista ao clicar de novo e avisa quando não há inscritos', async () => {
    mockApi({
      'GET /api/usuarios/me': [200, EU],
      'GET /api/desafios/meus': [200, DESAFIOS],
      'GET /api/candidaturas/desafio/4': [200, []],
    })
    renderTela()
    await screen.findByRole('heading', { name: 'Sem ninguém' })

    fireEvent.click(within(card('Sem ninguém')).getByRole('button', { name: 'Ver inscrições' }))
    expect(await screen.findByText('Ninguém se inscreveu ainda.')).toBeInTheDocument()

    fireEvent.click(within(card('Sem ninguém')).getByRole('button', { name: 'Esconder inscrições' }))
    expect(screen.queryByText('Ninguém se inscreveu ainda.')).not.toBeInTheDocument()
  })

  it('fecha e reabre as inscrições', async () => {
    const fetch = mockApi({
      'GET /api/usuarios/me': [200, EU],
      'GET /api/desafios/meus': [200, DESAFIOS],
      'PATCH /api/desafios/3/status': [200, {}],
    })
    renderTela()
    await screen.findByRole('heading', { name: 'API de pedidos' })

    fireEvent.click(within(card('API de pedidos')).getByRole('button', { name: 'Fechar inscrições' }))

    expect(await within(card('API de pedidos')).findByText('Encerrado')).toBeInTheDocument()
    const patch = fetch.mock.calls.find(([, opcoes]) => opcoes.method === 'PATCH')
    expect(JSON.parse(patch[1].body)).toEqual({ status: 'FECHADO' })

    fireEvent.click(within(card('API de pedidos')).getByRole('button', { name: 'Reabrir inscrições' }))
    expect(await within(card('API de pedidos')).findByRole('button', { name: 'Fechar inscrições' })).toBeInTheDocument()
  })

  it('só oferece apagar desafio sem inscritos, e pede confirmação', async () => {
    const confirmar = vi.spyOn(window, 'confirm').mockReturnValue(false)
    mockApi({
      'GET /api/usuarios/me': [200, EU],
      'GET /api/desafios/meus': [200, DESAFIOS],
      'DELETE /api/desafios/4': [204],
    })
    renderTela()
    await screen.findByRole('heading', { name: 'Sem ninguém' })

    expect(within(card('API de pedidos')).queryByRole('button', { name: 'Apagar' })).not.toBeInTheDocument()

    fireEvent.click(within(card('Sem ninguém')).getByRole('button', { name: 'Apagar' }))
    expect(screen.getByRole('heading', { name: 'Sem ninguém' })).toBeInTheDocument()

    confirmar.mockReturnValue(true)
    fireEvent.click(within(card('Sem ninguém')).getByRole('button', { name: 'Apagar' }))

    await vi.waitFor(() => expect(screen.queryByRole('heading', { name: 'Sem ninguém' })).not.toBeInTheDocument())
  })

  it('mostra o erro quando a ação é recusada', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true)
    mockApi({
      'GET /api/usuarios/me': [200, EU],
      'GET /api/desafios/meus': [200, DESAFIOS],
      'DELETE /api/desafios/4': [409, { erro: 'Este desafio já tem inscrições. Feche as inscrições em vez de apagar.' }],
    })
    renderTela()
    await screen.findByRole('heading', { name: 'Sem ninguém' })

    fireEvent.click(within(card('Sem ninguém')).getByRole('button', { name: 'Apagar' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Este desafio já tem inscrições')
    expect(screen.getByRole('heading', { name: 'Sem ninguém' })).toBeInTheDocument()
  })

  it('leva para publicar quando ainda não há desafios', async () => {
    mockApi({ 'GET /api/usuarios/me': [200, { ...EU, bio: null }], 'GET /api/desafios/meus': [200, []] })
    renderTela()

    expect(await screen.findByText('Você ainda não publicou nenhum desafio.')).toBeInTheDocument()
    fireEvent.click(screen.getByRole('button', { name: 'Publicar o primeiro desafio' }))

    expect(await screen.findByRole('heading', { name: 'Publique um desafio' })).toBeInTheDocument()
  })

  it('edita o perfil', async () => {
    mockApi({
      'GET /api/usuarios/me': [200, EU],
      'GET /api/desafios/meus': [200, []],
      'PUT /api/usuarios/me': [200, { ...EU, name: 'Rafael F.' }],
    })
    renderTela()

    fireEvent.click(await screen.findByRole('button', { name: 'Editar perfil' }))
    fireEvent.change(screen.getByLabelText('Nome'), { target: { value: 'Rafael F.' } })
    fireEvent.click(screen.getByRole('button', { name: 'Salvar perfil' }))

    expect(await screen.findByRole('heading', { name: 'Rafael F.' })).toBeInTheDocument()
  })

  it('volta para o login quando o token é recusado e avisa quando a API cai', async () => {
    mockApi({ 'GET /api/usuarios/me': [401, { erro: 'Faça login para continuar.' }], 'GET /api/desafios/meus': [401, {}] })
    renderTela()
    expect(await screen.findByRole('heading', { name: 'Entre na sua conta' })).toBeInTheDocument()
    cleanup()

    logarComo('MENTOR')
    mockApi({ 'GET /api/usuarios/me': [500, {}], 'GET /api/desafios/meus': [500, {}] })
    renderTela()
    expect(await screen.findByRole('alert')).toHaveTextContent('Não consegui carregar seu perfil')
  })
})
