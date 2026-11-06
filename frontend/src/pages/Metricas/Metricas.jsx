// Tela 9 do protótipo: painel de métricas (rota /metricas, aberta sem login)

import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router'
import ContaTopbar from '../../components/ContaTopbar/ContaTopbar'
import { buscarMetricas } from '../../services/usuarios'
import './Metricas.css'

const NOME_DO_MES = ['JAN', 'FEV', 'MAR', 'ABR', 'MAI', 'JUN', 'JUL', 'AGO', 'SET', 'OUT', 'NOV', 'DEZ']

// "2026-11" -> "NOV"
function rotuloDoMes(mes) {
  return NOME_DO_MES[Number(mes.slice(5, 7)) - 1]
}

function porcentagem(parte, total) {
  return total > 0 ? Math.round((parte / total) * 100) : 0
}

export default function Metricas() {
  const navigate = useNavigate()
  const [metricas, setMetricas] = useState(null)
  const [erro, setErro] = useState('')

  useEffect(() => {
    let ativo = true

    buscarMetricas()
      .then(dados => { if (ativo) setMetricas(dados) })
      .catch(() => { if (ativo) setErro('Não consegui carregar as métricas. Confira se o back-end está rodando.') })

    return () => { ativo = false }
  }, [])

  const maiorMes = metricas ? Math.max(1, ...metricas.inscricoesPorMes.map(m => m.total)) : 1

  return (
    <div className="surface-paper">
      <header className="topbar">
        <div className="crumb">TRAMPOLIM / <b>MÉTRICAS</b></div>
        <div className="top-actions">
          <span className="status grey-status">VISÃO PÚBLICA</span>
          <button type="button" className="plain-link" onClick={() => navigate('/student/search')}>Ver desafios</button>
          <ContaTopbar />
        </div>
      </header>

      <div className="page-header">
        <div>
          <h1>Movimento da comunidade</h1>
          <p>Os números que mostram quantas pessoas estão construindo, recebendo avaliação e transformando prática em portfólio.</p>
        </div>
      </div>

      {erro && <div className="empty-state" role="alert">{erro}</div>}

      {!metricas && !erro && <div className="desafios-loading">Carregando métricas…</div>}

      {metricas && (
        <div className="painel-pad">
          <div className="painel-numeros">
            <article className="painel-numero">
              <div>Desafios publicados</div>
              <strong>{metricas.desafios}</strong>
            </article>
            <article className="painel-numero">
              <div>Inscrições realizadas</div>
              <strong>{metricas.inscricoes}</strong>
            </article>
            <article className="painel-numero">
              <div>Taxa de conclusão</div>
              <strong>{metricas.taxaConclusao}%</strong>
            </article>
          </div>

          <div className="painel-graficos">
            <article className="painel-card">
              <h2>Inscrições por mês</h2>
              <p>Participações em desafios nos últimos 6 meses.</p>

              <div className="painel-barras" role="img"
                aria-label={metricas.inscricoesPorMes.map(m => `${rotuloDoMes(m.mes)}: ${m.total}`).join(', ')}>
                {metricas.inscricoesPorMes.map(m => (
                  <div className="painel-barra" key={m.mes}>
                    <b>{m.total}</b>
                    <i style={{ height: `${porcentagem(m.total, maiorMes)}%` }} />
                    <span>{rotuloDoMes(m.mes)}</span>
                  </div>
                ))}
              </div>
            </article>

            <article className="painel-card">
              <h2>Da inscrição à prova</h2>

              <div className="painel-funil">
                <div className="painel-funil-rotulo"><span>Inscrições</span><b>{metricas.inscricoes}</b></div>
                <div className="painel-funil-trilho"><div className="ambar" style={{ width: metricas.inscricoes > 0 ? '100%' : '0%' }} /></div>
              </div>
              <div className="painel-funil">
                <div className="painel-funil-rotulo"><span>Projetos enviados</span><b>{metricas.enviados}</b></div>
                <div className="painel-funil-trilho"><div className="ambar" style={{ width: `${porcentagem(metricas.enviados, metricas.inscricoes)}%` }} /></div>
              </div>
              <div className="painel-funil">
                <div className="painel-funil-rotulo"><span>Avaliações concluídas</span><b>{metricas.avaliados}</b></div>
                <div className="painel-funil-trilho"><div style={{ width: `${porcentagem(metricas.avaliados, metricas.inscricoes)}%` }} /></div>
              </div>

              <p className="painel-nota">
                {metricas.avaliados} de {metricas.inscricoes} inscrições já viraram trabalho avaliado por um mentor.
              </p>
            </article>
          </div>
        </div>
      )}
    </div>
  )
}
