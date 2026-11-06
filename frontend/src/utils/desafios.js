// Formatos que várias telas de desafio usam

export const LABEL_NIVEL = {
  INICIANTE: 'Iniciante',
  INTERMEDIARIO: 'Intermediário',
  AVANCADO: 'Avançado',
}

// A API guarda as tecnologias num texto só: "Java, Spring Boot" -> ['Java', 'Spring Boot']
export function listaDeTecnologias(texto) {
  return (texto ?? '').split(',').map(t => t.trim()).filter(Boolean)
}

// "2026-11-12" -> "12/11/2026". Sem new Date(): uma data pura seria lida como UTC e voltaria um dia no Brasil
export function formatarDia(data) {
  return data ? data.slice(0, 10).split('-').reverse().join('/') : '—'
}

// "2026-11-12T14:32:00" -> "12/11/2026, 14:32"
export function formatarDataHora(data) {
  if (!data) return '—'
  return new Date(data).toLocaleString('pt-BR', {
    day: '2-digit', month: '2-digit', year: 'numeric', hour: '2-digit', minute: '2-digit',
  })
}

// O prazo vale até o fim do próprio dia
export function prazoVencido(prazo) {
  if (!prazo) return false
  const hoje = new Date()
  const mes = String(hoje.getMonth() + 1).padStart(2, '0')
  const dia = String(hoje.getDate()).padStart(2, '0')
  return prazo < `${hoje.getFullYear()}-${mes}-${dia}`
}

// Etapa da inscrição, do ponto de vista de quem lê
export const LABEL_STATUS = {
  PENDENTE: 'Aguardando envio',
  APROVADO: 'Aguardando envio',
  REJEITADO: 'Não aprovada',
  ENVIADO: 'Aguardando avaliação',
  AVALIADO: 'Avaliada',
}
