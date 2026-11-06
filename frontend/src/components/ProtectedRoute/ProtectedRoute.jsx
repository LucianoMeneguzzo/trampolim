import { Navigate, Outlet, useLocation } from 'react-router'
import { getUser } from '../../services/api'
import { destinoInicial } from '../../services/auth'

// Envolve rotas que exigem login. Ex.: <Route element={<ProtectedRoute allow={['MENTOR']} />}>
//   sem sessão (ou token expirado)  -> /login, lembrando de onde a pessoa veio
//   logado com o perfil errado      -> tela inicial do próprio perfil
export default function ProtectedRoute({ allow }) {
  const location = useLocation()
  const user = getUser()

  if (!user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  if (allow && !allow.includes(user.type)) {
    return <Navigate to={destinoInicial(user.type)} replace />
  }

  return <Outlet />
}
