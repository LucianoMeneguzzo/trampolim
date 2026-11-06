// Só vira link o que começa com https://. A API já recusa outros endereços, mas o href é o último
// ponto antes do navegador: um "javascript:..." aqui rodaria código de quem abrisse o perfil.
export function linkSeguro(url) {
  return typeof url === 'string' && url.startsWith('https://') ? url : null
}

// "https://github.com/ana" -> "github.com/ana"
export function semProtocolo(url) {
  return url.replace(/^https?:\/\//, '')
}
