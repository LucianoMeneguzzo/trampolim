import { apiFetch } from './api'

// Valores aceitos pela API para o nível do desafio
export const NIVEIS = [
  { valor: 'INICIANTE', label: 'Iniciante' },
  { valor: 'INTERMEDIARIO', label: 'Intermediário' },
  { valor: 'AVANCADO', label: 'Avançado' },
]

// GET /api/desafios: abre sem login
export function listarDesafios() {
  return apiFetch('/api/desafios')
}

// GET /api/desafios/:id: abre sem login e traz o número de inscritos
export function buscarDesafio(id) {
  return apiFetch(`/api/desafios/${id}`)
}

// GET /api/desafios/meus: desafios do mentor logado, com inscritos e entregas
export function listarMeusDesafios() {
  return apiFetch('/api/desafios/meus')
}

// POST /api/desafios (só mentor). tecnologias é um texto ("Java, Spring Boot") e prazo uma data (AAAA-MM-DD)
export function criarDesafio({ titulo, descricao, tecnologias, nivel, prazo, vagas, empresa }) {
  return apiFetch('/api/desafios', {
    method: 'POST',
    body: JSON.stringify({ titulo, descricao, tecnologias, nivel, prazo, vagas, empresa }),
  })
}

// PUT /api/desafios/:id (só o mentor que publicou)
export function atualizarDesafio(id, { titulo, descricao, tecnologias, nivel, prazo, vagas, empresa }) {
  return apiFetch(`/api/desafios/${id}`, {
    method: 'PUT',
    body: JSON.stringify({ titulo, descricao, tecnologias, nivel, prazo, vagas, empresa }),
  })
}

// PATCH /api/desafios/:id/status: ABERTO recebe inscrições, FECHADO não
export function alterarStatusDesafio(id, status) {
  return apiFetch(`/api/desafios/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status }),
  })
}

// DELETE /api/desafios/:id: só funciona em desafio sem inscrições
export function apagarDesafio(id) {
  return apiFetch(`/api/desafios/${id}`, { method: 'DELETE' })
}

// POST /api/candidaturas (só aluno)
export function inscrever(desafioId) {
  return apiFetch('/api/candidaturas', {
    method: 'POST',
    body: JSON.stringify({ desafioId }),
  })
}

// GET /api/candidaturas/minhas: inscrições do aluno logado, com a avaliação quando houver
export function listarMinhasCandidaturas() {
  return apiFetch('/api/candidaturas/minhas')
}

// GET /api/candidaturas/desafio/:id (só o mentor que publicou)
export function listarCandidaturasDoDesafio(desafioId) {
  return apiFetch(`/api/candidaturas/desafio/${desafioId}`)
}

// PUT /api/candidaturas/:id/envio: o back confirma no GitHub que o repositório existe
export function enviarProjeto(candidaturaId, linkEnvio) {
  return apiFetch(`/api/candidaturas/${candidaturaId}/envio`, {
    method: 'PUT',
    body: JSON.stringify({ linkEnvio }),
  })
}

// GET /api/candidaturas/:id (mentor dono do desafio ou o próprio aluno)
export function buscarCandidatura(id) {
  return apiFetch(`/api/candidaturas/${id}`)
}

// POST /api/candidaturas/:id/avaliacao (só o mentor dono do desafio)
export function avaliarCandidatura(id, { nota, feedback }) {
  return apiFetch(`/api/candidaturas/${id}/avaliacao`, {
    method: 'POST',
    body: JSON.stringify({ nota, feedback }),
  })
}
