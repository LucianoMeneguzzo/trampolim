import { afterEach, describe, expect, it, vi } from 'vitest'
import { cleanup, fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import App from '../../App'
import { logarComo, mockApi } from '../../test/utils'

const PERFIL = {
  id: 5, name: 'Ana Martins', type: 'STUDENT', bio: 'Construo APIs.', professionalTitle: 'Dev em formação',
  github: 'https://github.com/ana', linkedin: 'https://linkedin.com/in/ana',
  trabalhos: [
    { candidaturaId: 1, tituloDesafio: 'API de pedidos', tecnologias: 'Java, Spring Boot', nomeMentor: 'Rafael', nota: 9, feedback: 'Bem organizado.', linkEnvio: 'https://github.com/ana/api', dataEnvio: new Date().toISOString() },
    { candidaturaId: 2, tituloDesafio: 'Landing page', tecnologias: 'HTML, Java', nomeMentor: 'Marina', nota: 8, feedback: 'Boa atenção ao celular.', linkEnvio: 'javascript:alert(1)', dataEnvio: new Date().toISOString() },
  ],
}

function renderEm(rota) {
  return render(
    <MemoryRouter initialEntries={[rota]}>
      <App />
    </MemoryRouter>
  )
}

afterEach(() => {
  cleanup()
  localStorage.clear()
  vi.unstubAllGlobals()
})

describe('Perfil do aluno', () => {
  it('abre sem login e mostra o portfólio com os números calculados', async () => {
    mockApi({ 'GET /api/usuarios/5': [200, PERFIL] })
    renderEm('/student/profile/5')

    expect(await screen.findByRole('heading', { name: 'Ana Martins' })).toBeInTheDocument()
    expect(screen.getByText('Dev em formação')).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'API de pedidos' })).toBeInTheDocument()
    expect(screen.getByText('8.5')).toBeInTheDocument()   // média
    expect(screen.getByText('02')).toBeInTheDocument()    // desafios
    expect(screen.getByText('03')).toBeInTheDocument()    // Java, Spring Boot e HTML, sem repetir
    expect(screen.queryByRole('button', { name: 'Editar perfil' })).not.toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: 'Minhas inscrições' })).not.toBeInTheDocument()
  })

  it('só transforma em link o que começa com https', async () => {
    mockApi({ 'GET /api/usuarios/5': [200, { ...PERFIL, github: 'javascript:alert(1)' }] })
    renderEm('/student/profile/5')

    await screen.findByRole('heading', { name: 'Ana Martins' })

    expect(screen.queryByText(/javascript/)).not.toBeInTheDocument()
    expect(screen.getByRole('link', { name: /linkedin\.com\/in\/ana/ })).toHaveAttribute('href', 'https://linkedin.com/in/ana')
    expect(screen.getAllByRole('link', { name: /Ver repositório/ })).toHaveLength(1)
  })

  it('o próprio perfil exige login', async () => {
    renderEm('/student/profile')

    expect(await screen.findByRole('heading', { name: 'Entre na sua conta' })).toBeInTheDocument()
  })

  it('o aluno vê as próprias inscrições e edita o perfil', async () => {
    localStorage.setItem('token', 'x')
    logarComo('STUDENT', 'Ana Martins')
    const salvo = { ...PERFIL, id: 1, name: 'Ana M.', bio: 'Nova bio.' }
    const fetch = mockApi({
      'GET /api/usuarios/1': [200, { ...PERFIL, id: 1 }],
      'GET /api/candidaturas/minhas': [200, [
        { id: 40, desafioId: 7, tituloDesafio: 'Agenda de consultas', status: 'PENDENTE' },
        { id: 41, desafioId: 8, tituloDesafio: 'Já avaliado', status: 'AVALIADO' },
      ]],
      'PUT /api/usuarios/me': [200, salvo],
    })
    renderEm('/student/profile')

    expect(await screen.findByRole('heading', { name: 'Minhas inscrições' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: 'Agenda de consultas' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Enviar projeto' })).toBeInTheDocument()
    expect(screen.queryByRole('heading', { name: 'Já avaliado' })).not.toBeInTheDocument()

    fireEvent.click(screen.getByRole('button', { name: 'Editar perfil' }))
    fireEvent.change(screen.getByLabelText('Nome'), { target: { value: 'Ana M.' } })
    fireEvent.change(screen.getByLabelText('Sobre você'), { target: { value: 'Nova bio.' } })
    fireEvent.click(screen.getByRole('button', { name: 'Salvar perfil' }))

    expect(await screen.findByRole('heading', { name: 'Ana M.' })).toBeInTheDocument()
    expect(screen.getByText('Nova bio.')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Salvar perfil' })).not.toBeInTheDocument()
    expect(JSON.parse(localStorage.getItem('user')).name).toBe('Ana M.')

    const put = fetch.mock.calls.find(([, opcoes]) => opcoes.method === 'PUT')
    expect(JSON.parse(put[1].body)).toMatchObject({ name: 'Ana M.', bio: 'Nova bio.', github: 'https://github.com/ana' })
  })

  it('mostra os erros de validação ao editar', async () => {
    logarComo('STUDENT', 'Ana Martins')
    mockApi({
      'GET /api/usuarios/1': [200, { ...PERFIL, id: 1 }],
      'GET /api/candidaturas/minhas': [200, []],
      'PUT /api/usuarios/me': [400, { github: 'Use o link do seu GitHub, ex: github.com/usuario' }],
    })
    renderEm('/student/profile')

    fireEvent.click(await screen.findByRole('button', { name: 'Editar perfil' }))
    fireEvent.change(screen.getByLabelText('GitHub'), { target: { value: 'javascript:alert(1)' } })
    fireEvent.click(screen.getByRole('button', { name: 'Salvar perfil' }))

    expect(await screen.findByText('Use o link do seu GitHub, ex: github.com/usuario')).toBeInTheDocument()
    expect(screen.getByRole('alert')).toHaveTextContent('Confira os campos destacados.')

    fireEvent.click(screen.getByRole('button', { name: 'Cancelar' }))
    expect(screen.queryByRole('button', { name: 'Salvar perfil' })).not.toBeInTheDocument()
  })

  it('perfil sem trabalhos explica o que vai aparecer', async () => {
    mockApi({ 'GET /api/usuarios/5': [200, { ...PERFIL, trabalhos: [], bio: null, professionalTitle: null }] })
    renderEm('/student/profile/5')

    expect(await screen.findByText('Os trabalhos avaliados por mentores vão aparecer aqui.')).toBeInTheDocument()
    expect(screen.getByText('ALUNO NO TRAMPOLIM')).toBeInTheDocument()
    expect(screen.getByText('Esta pessoa ainda não escreveu uma apresentação.')).toBeInTheDocument()
  })

  it('compartilhar copia o endereço público', async () => {
    const writeText = vi.fn().mockResolvedValue()
    vi.stubGlobal('navigator', { ...navigator, clipboard: { writeText } })
    mockApi({ 'GET /api/usuarios/5': [200, PERFIL] })
    renderEm('/student/profile/5')

    fireEvent.click(await screen.findByRole('button', { name: /Compartilhar perfil/ }))

    expect(await screen.findByRole('button', { name: /Link copiado/ })).toBeInTheDocument()
    expect(writeText.mock.calls[0][0]).toMatch(/\/student\/profile\/5$/)
  })

  it('explica quando o perfil não existe', async () => {
    mockApi({ 'GET /api/usuarios/5': [404, { erro: 'Perfil não encontrado.' }] })
    renderEm('/student/profile/5')

    expect(await screen.findByRole('alert')).toHaveTextContent('Este perfil não existe.')
  })
})
