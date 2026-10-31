package com.trampolim.api.dto;

import com.trampolim.api.entity.Candidatura;
import java.time.LocalDateTime;

public class CandidaturaRespostaDTO {

    private Long id;
    private String status;
    private LocalDateTime dataCandidatura;
    private String nomeCandidato;
    private String emailCandidato;
    private String tituloDesafio;
    private String linkEnvio;
    private LocalDateTime dataEnvio;

    public CandidaturaRespostaDTO() {}

    public CandidaturaRespostaDTO(Candidatura candidatura) {
        this.id = candidatura.getId();
        this.status = candidatura.getStatus();
        this.dataCandidatura = candidatura.getDataCandidatura();
        this.linkEnvio = candidatura.getLinkEnvio();
        this.dataEnvio = candidatura.getDataEnvio();

        if (candidatura.getCandidato() != null) {
            this.nomeCandidato = candidatura.getCandidato().getName();
            this.emailCandidato = candidatura.getCandidato().getEmail();
        } else {
            this.nomeCandidato = "Desconhecido";
            this.emailCandidato = "Desconhecido";
        }

        if (candidatura.getDesafio() != null) {
            this.tituloDesafio = candidatura.getDesafio().getTitulo();
        } else {
            this.tituloDesafio = "Desafio Desconhecido";
        }
    }

    // Getters
    public Long getId() { return id; }
    public String getStatus() { return status; }
    public LocalDateTime getDataCandidatura() { return dataCandidatura; }
    public String getNomeCandidato() { return nomeCandidato; }
    public String getEmailCandidato() { return emailCandidato; }
    public String getTituloDesafio() { return tituloDesafio; }
    public String getLinkEnvio() { return linkEnvio; }
    public LocalDateTime getDataEnvio() { return dataEnvio; }
}