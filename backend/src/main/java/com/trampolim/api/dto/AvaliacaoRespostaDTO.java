package com.trampolim.api.dto;

import com.trampolim.api.entity.Avaliacao;
import java.time.LocalDateTime;

public class AvaliacaoRespostaDTO {

    private Long id;
    private Integer nota;
    private String feedback;
    private LocalDateTime dataAvaliacao;
    private String nomeMentor;
    private String emailMentor;
    private Long candidaturaId;
    private String nomeCandidato;
    private String tituloDesafio;

    public AvaliacaoRespostaDTO() {}

    public AvaliacaoRespostaDTO(Avaliacao avaliacao) {
        this.id = avaliacao.getId();
        this.nota = avaliacao.getNota();
        this.feedback = avaliacao.getFeedback();
        this.dataAvaliacao = avaliacao.getDataAvaliacao();

        if (avaliacao.getMentor() != null) {
            this.nomeMentor = avaliacao.getMentor().getName();
            this.emailMentor = avaliacao.getMentor().getEmail();
        }

        if (avaliacao.getCandidatura() != null) {
            this.candidaturaId = avaliacao.getCandidatura().getId();
            if (avaliacao.getCandidatura().getCandidato() != null) {
                this.nomeCandidato = avaliacao.getCandidatura().getCandidato().getName();
            }
            if (avaliacao.getCandidatura().getDesafio() != null) {
                this.tituloDesafio = avaliacao.getCandidatura().getDesafio().getTitulo();
            }
        }
    }

    // Getters
    public Long getId() { return id; }
    public Integer getNota() { return nota; }
    public String getFeedback() { return feedback; }
    public LocalDateTime getDataAvaliacao() { return dataAvaliacao; }
    public String getNomeMentor() { return nomeMentor; }
    public String getEmailMentor() { return emailMentor; }
    public Long getCandidaturaId() { return candidaturaId; }
    public String getNomeCandidato() { return nomeCandidato; }
    public String getTituloDesafio() { return tituloDesafio; }
}