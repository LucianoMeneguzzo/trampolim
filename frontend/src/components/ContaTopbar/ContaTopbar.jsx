import { useNavigate } from 'react-router'
import { getUser } from '../../services/api'
import { destinoInicial, sair } from '../../services/auth'
import { iniciais } from '../../utils/iniciais'

// Lado direito da barra do topo nas telas escuras: "Entrar" para visitantes,
// "Meu perfil", "Sair" e o avatar para quem está logado.
export default function ContaTopbar() {
  const navigate = useNavigate()
  const usuario = getUser()

  function handleSair() {
    sair()
    navigate('/', { replace: true })
  }

  if (!usuario) {
    return <button type="button" className="plain-link" onClick={() => navigate('/login')}>Entrar</button>
  }

  return (
    <>
      <button type="button" className="plain-link" onClick={() => navigate(destinoInicial(usuario.type))}>Meu perfil</button>
      <button type="button" className="plain-link" onClick={handleSair}>Sair</button>
      <div className="avatar" title={usuario.name}>{iniciais(usuario.name)}</div>
    </>
  )
}
