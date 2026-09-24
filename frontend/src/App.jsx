// IMPORTAÇÕES FUNCIONAIS
import { Routes, Route } from 'react-router';

// COMPONENTES
import Home from './pages/Home/Home';
import PerfilPublico from './pages/PerfilAluno/PerfilAluno';

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/perfil" element={<PerfilPublico />} />
    </Routes>
  )
}
