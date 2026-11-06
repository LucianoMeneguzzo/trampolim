import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import App from '../../App'
import { logarComo, mockApi } from '../../test/utils'

const INSCRICAO = {
  id: 40, desafioId: 7, tituloDesafio: 'API de pedidos', status: 'PENDENTE', linkEnvio: null,
  repoLinguagem: null, repoUltimoCommit: null, dataEnvio: null, avaliacao: null,
}

function renderTela() {
  return render(
    <MemoryRouter initialEntries={['/student/challenge/7/send-challenge']}>
      <App />
    </MemoryRouter>
  )
}

beforeEach(() => logarComo('STUDENT', 'Ana Martins'))

afterEach(() => {
  cleanup()
  localStorage.clear()
  vi.unstubAllGlobals()
})

describe('Envio do projeto', () => {
  it('exige login de aluno', async () => {
    localStorage.clear()
    render(
      <MemoryRouter initialEntries={['/student/challenge/7/send-challenge']}>
        <App />
      </MemoryRouter>
    )

    expect(await screen.findByRole('heading', { name: 'Entre na sua conta' })).toBeInTheDocument()
  })

  it('envia o link e mostra o que o GitHub confirmou', async () => {
    const fetch = mockApi({
      'GET /api/candidaturas/minhas': [200, [INSCRICAO]],
      'PUT /api/candidaturas/40/envio': [200, {
        ...INSCRICAO,
        status: 'ENVIADO',
        linkEnvio: 'https://github.com/ana/api-pedidos',
        repoLinguagem: 'Java',
        repoUltimoCommit: '2026-10-20T15:30:00',
        dataEnvio: '2026-10-21T10:00:00',
      }],
    })
    renderTela()

    fireEvent.change(await screen.findByLabelText('URL do GitHub'), { target: { value: ' github.com/ana/api-pedidos ' } })
    fireEvent.click(screen.getByRole('button', { name: /Enviar para avaliação/ }))

    expect(await screen.findByText('Projeto enviado para o mentor.')).toBeInTheDocument()
    expect(screen.getByText('Java')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /Abrir repositório/ })).toHaveAttribute('href', 'https://github.com/ana/api-pedidos')
    expect(screen.getByRole('button', { name: /Atualizar envio/ })).toBeInTheDocument()

    const put = fetch.mock.calls.find(([, opcoes]) => opcoes.method === 'PUT')
    expect(JSON.parse(put[1].body)).toEqual({ linkEnvio: 'github.com/ana/api-pedidos' })
  })

  it('avisa quando o GitHub não pôde ser consultado', async () => {
    mockApi({
      'GET /api/candidaturas/minhas': [200, [{ ...INSCRICAO, status: 'ENVIADO', linkEnvio: 'https://github.com/ana/api', dataEnvio: '2026-10-21T10:00:00' }]],
    })
    renderTela()

    expect(await screen.findByText(/Não consegui consultar os dados no GitHub agora/)).toBeInTheDocument()
    expect(screen.getByLabelText('URL do GitHub')).toHaveValue('github.com/ana/api')
  })

  it('mostra quando o repositório não existe no GitHub', async () => {
    mockApi({
      'GET /api/candidaturas/minhas': [200, [INSCRICAO]],
      'PUT /api/candidaturas/40/envio': [400, { erro: 'Não encontramos esse repositório no GitHub. Confira o link e se ele é público.' }],
    })
    renderTela()

    fireEvent.change(await screen.findByLabelText('URL do GitHub'), { target: { value: 'github.com/ana/nao-existe' } })
    fireEvent.click(screen.getByRole('button', { name: /Enviar para avaliação/ }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Não encontramos esse repositório no GitHub')
  })

  it('mostra o erro de validação do link ao lado do campo', async () => {
    mockApi({
      'GET /api/candidaturas/minhas': [200, [INSCRICAO]],
      'PUT /api/candidaturas/40/envio': [400, { linkEnvio: 'Use o link de um repositório do GitHub, ex: github.com/usuario/projeto' }],
    })
    renderTela()

    fireEvent.change(await screen.findByLabelText('URL do GitHub'), { target: { value: 'exemplo.com' } })
    fireEvent.click(screen.getByRole('button', { name: /Enviar para avaliação/ }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Use o link de um repositório do GitHub')
  })

  it('manda para o desafio quem ainda não se inscreveu', async () => {
    mockApi({ 'GET /api/candidaturas/minhas': [200, []] })
    renderTela()

    expect(await screen.findByText('Você ainda não se inscreveu neste desafio.')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Ver o desafio' })).toBeInTheDocument()
  })

  it('mostra a nota e o feedback depois da avaliação, sem formulário', async () => {
    mockApi({
      'GET /api/candidaturas/minhas': [200, [{
        ...INSCRICAO,
        status: 'AVALIADO',
        linkEnvio: 'https://github.com/ana/api',
        avaliacao: { nota: 9, feedback: 'Muito bem organizado.', nomeMentor: 'Rafael', dataAvaliacao: '2026-10-25T10:00:00' },
      }]],
    })
    renderTela()

    expect(await screen.findByRole('heading', { name: 'Seu projeto foi avaliado' })).toBeInTheDocument()
    expect(screen.getByText('Muito bem organizado.')).toBeInTheDocument()
    expect(screen.queryByLabelText('URL do GitHub')).not.toBeInTheDocument()
  })

  it('não oferece envio em inscrição rejeitada', async () => {
    mockApi({ 'GET /api/candidaturas/minhas': [200, [{ ...INSCRICAO, status: 'REJEITADO' }]] })
    renderTela()

    expect(await screen.findByRole('heading', { name: 'Inscrição não aprovada' })).toBeInTheDocument()
    expect(screen.queryByLabelText('URL do GitHub')).not.toBeInTheDocument()
  })

  it('avisa quando a API está fora do ar', async () => {
    mockApi({ 'GET /api/candidaturas/minhas': [500, { erro: 'Falhou' }] })
    renderTela()

    expect(await screen.findByRole('alert')).toHaveTextContent('Não consegui carregar sua inscrição')
  })
})
