import { apiFetch } from './api'

// Valores aceitos pela API para o nível do desafio
export const NIVEIS = [
  { valor: 'INICIANTE', label: 'Iniciante' },
  { valor: 'INTERMEDIARIO', label: 'Intermediário' },
  { valor: 'AVANCADO', label: 'Avançado' },
]

// POST /api/desafios (só mentor)
export function criarDesafio({ titulo, descricao, nivel, tecnologias, prazoDias, vagas, empresa }) {
  return apiFetch('/api/desafios', {
    method: 'POST',
    body: JSON.stringify({
      title: titulo,
      description: descricao,
      level: nivel,
      technologies: tecnologias,
      deadlineDays: prazoDias,
      slots: vagas,
      companyName: empresa || null,
    }),
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
