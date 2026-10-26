import trampolimLogo from '../../images/trampolim.png'
import { useNavigate } from 'react-router' 
import './Logo.css'

export default function Logo() {
    const navigate = useNavigate()

    return (
        <button type="button" className="brand brand-button" onClick={() => navigate('/')}>
            <img className="logo" src={trampolimLogo} alt="logotipo da Trampolim" /> Trampolim
        </button>
    )
}