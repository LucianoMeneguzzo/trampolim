import { Routes, Route } from 'react-router-dom';
import Layout from './components/Layout.jsx';
import Home from './pages/Home.jsx';
import Login from './pages/Login.jsx';
import Desafios from './pages/Desafios.jsx';
import DesafioDetalhe from './pages/DesafioDetalhe.jsx';
import Envio from './pages/Envio.jsx';
import CriarDesafio from './pages/CriarDesafio.jsx';
import Avaliar from './pages/Avaliar.jsx';
import Perfil from './pages/Perfil.jsx';
import Painel from './pages/Painel.jsx';

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/desafios" element={<Desafios />} />
        <Route path="/desafios/novo" element={<CriarDesafio />} />
        <Route path="/desafios/:id" element={<DesafioDetalhe />} />
        <Route path="/desafios/:id/enviar" element={<Envio />} />
        <Route path="/avaliar/:id" element={<Avaliar />} />
        <Route path="/perfil/:id" element={<Perfil />} />
        <Route path="/painel" element={<Painel />} />
      </Route>
    </Routes>
  );
}
