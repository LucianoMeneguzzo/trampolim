import { Routes, Route, Outlet, Navigate } from 'react-router'

import ProtectedRoute   from './components/ProtectedRoute/ProtectedRoute'
import Home             from './pages/Home/Home'
import Login            from './pages/Login/Login'
import Cadastro         from './pages/Cadastro/Cadastro'
import PerfilAluno      from './pages/PerfilAluno/PerfilAluno'
import DetalheDesafio   from './pages/DetalheDesafio/DetalheDesafio'
import Desafio          from './pages/Desafio/Desafio'
import EnviarDesafio    from './pages/EnviarDesafio/EnviarDesafio'
import PerfilEmpresa    from './pages/PerfilEmpresa/PerfilEmpresa'
import PostarDesafio    from './pages/PostarDesafio/PostarDesafio'
import AvaliarDesafio   from './pages/AvaliarDesafio/AvaliarDesafio'
import Metricas         from './pages/Metricas/Metricas'

function StudentLayout() {
  return <Outlet />
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Cadastro />} />
      <Route path="/metricas" element={<Metricas />} />

      {/* Atalho para a lista de desafios */}
      <Route path="/desafios" element={<Navigate to="/student/search" replace />} />

      {/* Área do aluno: lista, detalhe e portfólio são públicos; o próprio perfil e o envio exigem login de aluno */}
      <Route path="/student" element={<StudentLayout />}>
        <Route path="search" element={<Desafio />} />
        <Route path="challenge/:id" element={<DetalheDesafio />} />
        <Route path="profile/:id" element={<PerfilAluno />} />
        <Route element={<ProtectedRoute allow={['STUDENT']} />}>
          <Route path="profile" element={<PerfilAluno />} />
          <Route path="challenge/:id/send-challenge" element={<EnviarDesafio />} />
        </Route>
      </Route>

      {/* Área do mentor: tudo exige login com perfil MENTOR */}
      <Route path="/business" element={<ProtectedRoute allow={['MENTOR']} />}>
        <Route path="profile" element={<PerfilEmpresa />} />
        <Route path="post-challenge" element={<PostarDesafio />} />
        <Route path="challenge/:id/edit" element={<PostarDesafio />} />
        <Route path="assessment/:id" element={<AvaliarDesafio />} />
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
