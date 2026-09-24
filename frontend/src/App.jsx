// IMPORTAÇÕES FUNCIONAIS
import { Routes, Route } from 'react-router';

// COMPONENTES
import Home             from './pages/Home/Home'
import PerfilAluno      from './pages/PerfilAluno/PerfilAluno'

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/student">
        <Route path="profile" element={<PerfilAluno />} />
      </Route>
    </Routes>
  )
}
