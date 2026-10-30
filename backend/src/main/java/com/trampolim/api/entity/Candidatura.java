package com.trampolim.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

// Inscrição de um aluno em um desafio (tabela "inscricoes" do plano técnico).
// Fluxo do status: PENDENTE -> (APROVADO | REJEITADO) -> ENVIADO -> AVALIADO
@Entity
@Table(name = "candidaturas")
public class Candidatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // Ex: "PENDENTE", "APROVADO", "REJEITADO", "ENVIADO", "AVALIADO"

    @Column(name = "data_candidatura", nullable = false)
    private LocalDateTime dataCandidatura;

    // Envio do projeto (preenchido quando o aluno manda o link do repositório)
    @Column(name = "link_repositorio", length = 255)
    private String linkRepositorio;

    // Vêm da API do GitHub na hora do envio (integração ainda a fazer)
    @Column(name = "repo_ultimo_commit")
    private LocalDateTime repoUltimoCommit;

    @Column(name = "repo_linguagem", length = 60)
    private String repoLinguagem;

    @Column(name = "data_envio")
    private LocalDateTime dataEnvio;

    // Relacionamento com o Usuário que se candidatou
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidato_id", nullable = false)
    @JsonIgnore // Evita loop infinito na serialização JSON
    private User candidato;

    // Relacionamento com o Desafio alvo da candidatura
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "desafio_id", nullable = false)
    @JsonIgnore // Evita loop infinito na serialização JSON
    private Challenge desafio;

    // Construtor vazio (exigido pelo JPA)
    public Candidatura() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDataCandidatura() { return dataCandidatura; }
    public void setDataCandidatura(LocalDateTime dataCandidatura) { this.dataCandidatura = dataCandidatura; }

    public String getLinkRepositorio() { return linkRepositorio; }
    public void setLinkRepositorio(String linkRepositorio) { this.linkRepositorio = linkRepositorio; }

    public LocalDateTime getRepoUltimoCommit() { return repoUltimoCommit; }
    public void setRepoUltimoCommit(LocalDateTime repoUltimoCommit) { this.repoUltimoCommit = repoUltimoCommit; }

    public String getRepoLinguagem() { return repoLinguagem; }
    public void setRepoLinguagem(String repoLinguagem) { this.repoLinguagem = repoLinguagem; }

    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }

    public User getCandidato() { return candidato; }
    public void setCandidato(User candidato) { this.candidato = candidato; }

    public Challenge getDesafio() { return desafio; }
    public void setDesafio(Challenge desafio) { this.desafio = desafio; }
}
