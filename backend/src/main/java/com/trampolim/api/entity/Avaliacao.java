package com.trampolim.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// Nota + feedback que o mentor dá para uma entrega. Cada candidatura tem no máximo uma avaliação.
@Entity
@Table(name = "avaliacoes")
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "candidatura_id", nullable = false, unique = true)
    @JsonIgnore
    private Candidatura candidatura;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mentor_id", nullable = false)
    @JsonIgnore
    private User mentor;

    @Column(name = "nota", nullable = false, precision = 3, scale = 1)
    private BigDecimal nota; // 0.0 a 10.0

    @Column(name = "feedback", columnDefinition = "TEXT", nullable = false)
    private String feedback;

    @Column(name = "data_avaliacao", nullable = false)
    private LocalDateTime dataAvaliacao;

    // Construtor vazio (exigido pelo JPA)
    public Avaliacao() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Candidatura getCandidatura() { return candidatura; }
    public void setCandidatura(Candidatura candidatura) { this.candidatura = candidatura; }

    public User getMentor() { return mentor; }
    public void setMentor(User mentor) { this.mentor = mentor; }

    public BigDecimal getNota() { return nota; }
    public void setNota(BigDecimal nota) { this.nota = nota; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public LocalDateTime getDataAvaliacao() { return dataAvaliacao; }
    public void setDataAvaliacao(LocalDateTime dataAvaliacao) { this.dataAvaliacao = dataAvaliacao; }
}
