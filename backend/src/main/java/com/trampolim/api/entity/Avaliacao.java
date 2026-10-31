package com.trampolim.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "avaliacoes")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidatura_id", nullable = false, unique = true)
    @JsonIgnore
    private Candidatura candidatura;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    @JsonIgnore
    private User mentor;

    @Column(nullable = false)
    private Integer nota; // 0 a 10

    @Column(nullable = false, length = 2000)
    private String feedback;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataAvaliacao;

    public Avaliacao() {
        this.dataAvaliacao = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Candidatura getCandidatura() { return candidatura; }
    public void setCandidatura(Candidatura candidatura) { this.candidatura = candidatura; }

    public User getMentor() { return mentor; }
    public void setMentor(User mentor) { this.mentor = mentor; }

    public Integer getNota() { return nota; }
    public void setNota(Integer nota) { this.nota = nota; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public LocalDateTime getDataAvaliacao() { return dataAvaliacao; }
    public void setDataAvaliacao(LocalDateTime dataAvaliacao) { this.dataAvaliacao = dataAvaliacao; }
}