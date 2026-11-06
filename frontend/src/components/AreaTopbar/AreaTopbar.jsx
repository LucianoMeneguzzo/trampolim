import { useNavigate } from 'react-router'
import { getUser } from '../../services/api'
import { sair } from '../../services/auth'
import { iniciais } from '../../utils/iniciais'
import './AreaTopbar.css'

// Barra do topo das telas logadas: "ÁREA DO MENTOR / NOVO DESAFIO", ações, botão Sair e avatar
export default function AreaTopbar({ area = 'ÁREA DO MENTOR', pagina, children }) {
  const navigate = useNavigate()
  const usuario = getUser()

  function handleSair() {
    sair()
    navigate('/login', { replace: true })
  }

  return (
    <header className="area-topbar">
      <div className="area-crumb">
        {area} / <b>{pagina}</b>
      </div>
      <div className="area-actions">
        {children}
        <button type="button" className="area-link" onClick={handleSair}>Sair</button>
        <div className="area-avatar" title={usuario?.name}>{iniciais(usuario?.name)}</div>
      </div>
    </header>
  )
}
