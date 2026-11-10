package com.trampolim.api.dto;

import java.util.List;

// Resposta de GET /api/metricas (painel público)
public class MetricasDTO {

    private long desafios;
    private long inscricoes;
    private long enviados;
    private long avaliados;
    private int taxaConclusao; // % das inscrições que chegaram à avaliação
    private List<InscricoesNoMes> inscricoesPorMes;

    public record InscricoesNoMes(String mes, long total) {} // mes no formato AAAA-MM

    public MetricasDTO(long desafios, long inscricoes, long enviados, long avaliados, List<InscricoesNoMes> inscricoesPorMes) {
        this.desafios = desafios;
        this.inscricoes = inscricoes;
        this.enviados = enviados;
        this.avaliados = avaliados;
        this.taxaConclusao = inscricoes == 0 ? 0 : Math.round(avaliados * 100f / inscricoes);
        this.inscricoesPorMes = inscricoesPorMes;
    }

    // Getters
    public long getDesafios() { return desafios; }
    public long getInscricoes() { return inscricoes; }
    public long getEnviados() { return enviados; }
    public long getAvaliados() { return avaliados; }
    public int getTaxaConclusao() { return taxaConclusao; }
    public List<InscricoesNoMes> getInscricoesPorMes() { return inscricoesPorMes; }
}
