package com.trampolim.api.dto;

import com.trampolim.api.entity.Avaliacao;
import com.trampolim.api.entity.Candidatura;
import java.time.LocalDateTime;

// Tudo que a tela "Avalie a entrega" (/business/assessment/:id) precisa numa só chamada
public class CandidaturaDetalheDTO {

    private Long id;
    private String status;
    private LocalDateTime dataCandidatura;
    private LocalDateTime dataEnvio;
    private String linkRepositorio;
    private String repoLinguagem;
    private LocalDateTime repoUltimoCommit;

    private Long candidatoId;
    private String nomeCandidato;
    private String githubCandidato;

    private Long desafioId;
    private String tituloDesafio;
    private Integer prazoDias;

    private AvaliacaoRespostaDTO avaliacao; // null enquanto não foi avaliada

    public CandidaturaDetalheDTO() {}

    public CandidaturaDetalheDTO(Candidatura candidatura, Avaliacao avaliacao) {
        this.id = candidatura.getId();
        this.status = candidatura.getStatus();
        this.dataCandidatura = candidatura.getDataCandidatura();
        this.dataEnvio = candidatura.getDataEnvio();
        this.linkRepositorio = candidatura.getLinkRepositorio();
        this.repoLinguagem = candidatura.getRepoLinguagem();
        this.repoUltimoCommit = candidatura.getRepoUltimoCommit();

        this.candidatoId = candidatura.getCandidato().getId();
        this.nomeCandidato = candidatura.getCandidato().getName();
        this.githubCandidato = candidatura.getCandidato().getGithub();

        this.desafioId = candidatura.getDesafio().getId();
        this.tituloDesafio = candidatura.getDesafio().getTitle();
        this.prazoDias = candidatura.getDesafio().getDeadlineDays();

        this.avaliacao = avaliacao != null ? new AvaliacaoRespostaDTO(avaliacao) : null;
    }

    // Getters
    public Long getId() { return id; }
    public String getStatus() { return status; }
    public LocalDateTime getDataCandidatura() { return dataCandidatura; }
    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public String getLinkRepositorio() { return linkRepositorio; }
    public String getRepoLinguagem() { return repoLinguagem; }
    public LocalDateTime getRepoUltimoCommit() { return repoUltimoCommit; }
    public Long getCandidatoId() { return candidatoId; }
    public String getNomeCandidato() { return nomeCandidato; }
    public String getGithubCandidato() { return githubCandidato; }
    public Long getDesafioId() { return desafioId; }
    public String getTituloDesafio() { return tituloDesafio; }
    public Integer getPrazoDias() { return prazoDias; }
    public AvaliacaoRespostaDTO getAvaliacao() { return avaliacao; }
}
