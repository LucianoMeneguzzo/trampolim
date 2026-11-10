import { apiFetch } from './api'

// GET /api/usuarios/:id: perfil público, abre sem login e não traz e-mail
export function buscarPerfilPublico(id) {
  return apiFetch(`/api/usuarios/${id}`)
}

// PUT /api/usuarios/me: o id vem do token, cada pessoa só edita o próprio perfil
export function atualizarMeuPerfil({ name, professionalTitle, bio, github, linkedin }) {
  return apiFetch('/api/usuarios/me', {
    method: 'PUT',
    body: JSON.stringify({ name, professionalTitle, bio, github, linkedin }),
  })
}

// GET /api/metricas: números do painel público
export function buscarMetricas() {
  return apiFetch('/api/metricas')
}
