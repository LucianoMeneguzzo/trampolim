package com.trampolim.api.dto;

import com.trampolim.api.entity.Avaliacao;
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
    private String repoLinguagem;
    private LocalDateTime repoUltimoCommit;
    private AvaliacaoRespostaDTO avaliacao; // só vem preenchida em GET /api/candidaturas/{id}, depois da avaliação

    public CandidaturaRespostaDTO() {}

    public CandidaturaRespostaDTO(Candidatura candidatura) {
        this.id = candidatura.getId();
        this.status = candidatura.getStatus();
        this.dataCandidatura = candidatura.getDataCandidatura();
        this.linkEnvio = candidatura.getLinkEnvio();
        this.dataEnvio = candidatura.getDataEnvio();
        this.repoLinguagem = candidatura.getRepoLinguagem();
        this.repoUltimoCommit = candidatura.getRepoUltimoCommit();

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

    // Tudo que a tela "Avalie a entrega" (/business/assessment/:id) precisa numa só chamada
    public CandidaturaRespostaDTO(Candidatura candidatura, Avaliacao avaliacao) {
        this(candidatura);
        this.avaliacao = avaliacao != null ? new AvaliacaoRespostaDTO(avaliacao) : null;
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
    public String getRepoLinguagem() { return repoLinguagem; }
    public LocalDateTime getRepoUltimoCommit() { return repoUltimoCommit; }
    public AvaliacaoRespostaDTO getAvaliacao() { return avaliacao; }
}
