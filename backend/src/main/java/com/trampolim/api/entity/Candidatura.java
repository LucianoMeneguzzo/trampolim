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

    @Column(name = "status", nullable = false, length = 20)
    private String status; // Ex: "PENDENTE", "APROVADO", "REJEITADO"

    @Column(name = "data_candidatura", nullable = false)
    private LocalDateTime dataCandidatura;

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

    public User getCandidato() { return candidato; }
    public void setCandidato(User candidato) { this.candidato = candidato; }

    public Challenge getDesafio() { return desafio; }
    public void setDesafio(Challenge desafio) { this.desafio = desafio; }
}