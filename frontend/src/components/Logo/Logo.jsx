import trampolimLogo from '../../images/trampolim.png'
import './Logo.css'

export default function Logo() {

    return (
        <button type="button" className="brand brand-button" onClick={() => navigate('/')}>
            <img className="logo" src={trampolimLogo} alt="logotipo da Trampolim" /> Trampolim
        </button>
    )
}