import { afterEach, describe, expect, it, vi } from 'vitest'
import { formatarDia, formatarDataHora, listaDeTecnologias, prazoVencido } from './desafios'
import { iniciais } from './iniciais'
import { linkSeguro, semProtocolo } from './links'

afterEach(() => vi.useRealTimers())

describe('desafios', () => {
  it('separa as tecnologias e ignora o que está vazio', () => {
    expect(listaDeTecnologias('Java,  Spring Boot, ')).toEqual(['Java', 'Spring Boot'])
    expect(listaDeTecnologias(null)).toEqual([])
  })

  it('formata datas sem andar um dia por causa do fuso', () => {
    expect(formatarDia('2026-11-12')).toBe('12/11/2026')
    expect(formatarDia('2026-11-12T23:30:00')).toBe('12/11/2026')
    expect(formatarDia(null)).toBe('—')
    expect(formatarDataHora(null)).toBe('—')
    expect(formatarDataHora('2026-11-12T14:32:00')).toContain('12/11/2026')
  })

  it('o prazo vale até o fim do próprio dia', () => {
    vi.useFakeTimers({ toFake: ['Date'] })
    vi.setSystemTime(new Date(2026, 10, 12, 23, 30))

    expect(prazoVencido('2026-11-11')).toBe(true)
    expect(prazoVencido('2026-11-12')).toBe(false)
    expect(prazoVencido('2026-11-13')).toBe(false)
    expect(prazoVencido(null)).toBe(false)
  })
})

describe('links', () => {
  it('só aceita https', () => {
    expect(linkSeguro('https://github.com/ana')).toBe('https://github.com/ana')
    expect(linkSeguro('http://github.com/ana')).toBeNull()
    expect(linkSeguro('javascript:alert(1)')).toBeNull()
    expect(linkSeguro(' javascript:alert(1)')).toBeNull()
    expect(linkSeguro(null)).toBeNull()
  })

  it('tira o protocolo para exibir', () => {
    expect(semProtocolo('https://github.com/ana')).toBe('github.com/ana')
  })
})

describe('iniciais', () => {
  it('usa a primeira e a última palavra', () => {
    expect(iniciais('Ana Maria Martins')).toBe('AM')
    expect(iniciais('Rafael')).toBe('R')
    expect(iniciais('')).toBe('?')
    expect(iniciais()).toBe('?')
  })
})
