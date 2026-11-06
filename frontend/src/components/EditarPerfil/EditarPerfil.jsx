import { useState } from 'react'
import { atualizarUsuarioDaSessao } from '../../services/api'
import { atualizarMeuPerfil } from '../../services/usuarios'
import './EditarPerfil.css'

// Formulário de edição do próprio perfil, usado no perfil do aluno e no do mentor
export default function EditarPerfil({ perfil, onSalvo, onCancelar }) {
  const [form, setForm] = useState({
    name: perfil.name ?? '',
    professionalTitle: perfil.professionalTitle ?? '',
    bio: perfil.bio ?? '',
    github: perfil.github ?? '',
    linkedin: perfil.linkedin ?? '',
  })
  const [erros, setErros] = useState({})
  const [erro, setErro] = useState('')
  const [salvando, setSalvando] = useState(false)

  function refreshForm(e) {
    const { name, value } = e.target
    setForm(f => ({ ...f, [name]: value }))
  }

  async function whenSubmit(e) {
    e.preventDefault()
    setErro('')
    setErros({})
    setSalvando(true)

    try {
      const salvo = await atualizarMeuPerfil(form)
      atualizarUsuarioDaSessao({ name: salvo.name })
      onSalvo(salvo)
    } catch (err) {
      if (err instanceof TypeError) {
        setErro('Não consegui falar com o servidor. Confira se o back-end está rodando.')
      } else if (Object.keys(err.campos ?? {}).length) {
        setErros(err.campos)
        setErro('Confira os campos destacados.')
      } else {
        setErro(err.message || 'Não foi possível salvar agora. Tente de novo em instantes.')
      }
      setSalvando(false)
    }
  }

  function campo(name, label, props = {}) {
    return (
      <div className="field">
        <label htmlFor={`perfil-${name}`}>{label}</label>
        <input id={`perfil-${name}`} name={name} value={form[name]} onChange={refreshForm} {...props} />
        {erros[name] && <span className="field-error">{erros[name]}</span>}
      </div>
    )
  }

  return (
    <form className="editar-perfil" onSubmit={whenSubmit}>
      <h2>Editar perfil</h2>
      {erro && <p className="editar-perfil-erro" role="alert">{erro}</p>}

      {campo('name', 'Nome', { required: true, maxLength: 100 })}
      {campo('professionalTitle', 'Título profissional', { maxLength: 100, placeholder: 'Ex.: Desenvolvedora full stack em formação' })}

      <div className="field">
        <label htmlFor="perfil-bio">Sobre você</label>
        <textarea id="perfil-bio" name="bio" value={form.bio} onChange={refreshForm} maxLength={2000}
          placeholder="O que você faz e o que está construindo." />
        {erros.bio && <span className="field-error">{erros.bio}</span>}
      </div>

      {campo('github', 'GitHub', { maxLength: 150, placeholder: 'github.com/usuario' })}
      {campo('linkedin', 'LinkedIn', { maxLength: 150, placeholder: 'linkedin.com/in/usuario' })}

      <div className="editar-perfil-acoes">
        <button type="submit" className="btn btn-primary" disabled={salvando}>{salvando ? 'Salvando…' : 'Salvar perfil'}</button>
        <button type="button" className="plain-link" onClick={onCancelar}>Cancelar</button>
      </div>
    </form>
  )
}
