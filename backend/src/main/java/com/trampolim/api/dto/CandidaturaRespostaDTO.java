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

    // Construtor vazio
    public CandidaturaRespostaDTO() {}

    // Construtor inteligente: recebe a entidade Candidatura e mapeia apenas o que queremos mostrar
    public CandidaturaRespostaDTO(Candidatura candidatura) {
        this.id = candidatura.getId();
        this.status = candidatura.getStatus();
        this.dataCandidatura = candidatura.getDataCandidatura();

        // Verifica se existe um candidato vinculado
        if (candidatura.getCandidato() != null) {
            this.nomeCandidato = candidatura.getCandidato().getName();
            this.emailCandidato = candidatura.getCandidato().getEmail();
        } else {
            this.nomeCandidato = "Desconhecido";
            this.emailCandidato = "Desconhecido";
        }

        // Verifica se existe um desafio vinculado
        if (candidatura.getDesafio() != null) {
            this.tituloDesafio = candidatura.getDesafio().getTitle();
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
}