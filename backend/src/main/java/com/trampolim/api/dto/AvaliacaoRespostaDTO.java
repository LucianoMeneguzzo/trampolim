package com.trampolim.api.dto;

import com.trampolim.api.entity.Avaliacao;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AvaliacaoRespostaDTO {

    private Long id;
    private Long candidaturaId;
    private BigDecimal nota;
    private String feedback;
    private LocalDateTime dataAvaliacao;
    private String nomeMentor;

    public AvaliacaoRespostaDTO() {}

    public AvaliacaoRespostaDTO(Avaliacao avaliacao) {
        this.id = avaliacao.getId();
        this.candidaturaId = avaliacao.getCandidatura().getId();
        this.nota = avaliacao.getNota();
        this.feedback = avaliacao.getFeedback();
        this.dataAvaliacao = avaliacao.getDataAvaliacao();
        this.nomeMentor = avaliacao.getMentor().getName();
    }

    // Getters
    public Long getId() { return id; }
    public Long getCandidaturaId() { return candidaturaId; }
    public BigDecimal getNota() { return nota; }
    public String getFeedback() { return feedback; }
    public LocalDateTime getDataAvaliacao() { return dataAvaliacao; }
    public String getNomeMentor() { return nomeMentor; }
}
