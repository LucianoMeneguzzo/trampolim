// IMPORTAÇÕES FUNCIONAIS
import { Routes, Route } from 'react-router';

// COMPONENTES
import { Home } from './pages/Home/Home'

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
    </Routes>
  )
}
