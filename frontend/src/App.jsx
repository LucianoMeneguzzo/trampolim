// IMPORTAÇÕES FUNCIONAIS
import { Routes, Route } from 'react-router'

// COMPONENTES
import Home             from './pages/Home/Home'
import Login            from './pages/Login/Login'
import Cadastro         from './pages/Cadastro/Cadastro'
import PerfilAluno      from './pages/PerfilAluno/PerfilAluno'
import PesquisarDesafio from './pages/PesquisarDesafio/PesquisarDesafio'
import Desafio          from './pages/Desafio/Desafio'
import EnviarDesafio    from './pages/EnviarDesafio/EnviarDesafio'
import PerfilEmpresa    from './pages/PerfilEmpresa/PerfilEmpresa'
import PostarDesafio    from './pages/PostarDesafio/PostarDesafio'
import AvaliarDesafio   from './pages/AvaliarDesafio/AvaliarDesafio'

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Cadastro />} />

      <Route path="/student">
        <Route path="profile" element={<PerfilAluno />} />
        <Route path="search" element={<PesquisarDesafio />} />
        <Route path="challenge/:id" element={<Desafio />} />
        <Route path="challenge/:id/send-challenge" element={<EnviarDesafio />} />
      </Route>

      <Route path="/business">
        <Route path="profile" element={<PerfilEmpresa />} />
        <Route path="post-challenge" element={<PostarDesafio />} />
        <Route path="assessment/:id" element={<AvaliarDesafio />} />
      </Route>
    </Routes>
  )
}
