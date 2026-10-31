package com.trampolim.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidaturas")
public class Candidatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidato_id", nullable = false)
    @JsonIgnore
    private User candidato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "desafio_id", nullable = false)
    @JsonIgnore
    private Challenge desafio;

    @Column(nullable = false)
    private String status; // PENDENTE, APROVADO, REJEITADO, ENVIADO

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCandidatura;

    // Novos campos para o envio do projeto pelo aluno
    @Column(length = 500)
    private String linkEnvio;

    private LocalDateTime dataEnvio;

    public Candidatura() {
        this.status = "PENDENTE";
        this.dataCandidatura = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getCandidato() { return candidato; }
    public void setCandidato(User candidato) { this.candidato = candidato; }

    public Challenge getDesafio() { return desafio; }
    public void setDesafio(Challenge desafio) { this.desafio = desafio; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getDataCandidatura() { return dataCandidatura; }
    public void setDataCandidatura(LocalDateTime dataCandidatura) { this.dataCandidatura = dataCandidatura; }

    public String getLinkEnvio() { return linkEnvio; }
    public void setLinkEnvio(String linkEnvio) { this.linkEnvio = linkEnvio; }

    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }
}