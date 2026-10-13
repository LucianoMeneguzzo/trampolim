package com.trampolim.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "desafios")
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", nullable = false, length = 150)
    private String title;

    @Column(name = "descricao", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "nivel", nullable = false)
    private String level; // Ex: "INICIANTE", "INTERMEDIARIO", "AVANCADO"

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime createdAt;

    // Relacionamento com o Usuário (quem criou o desafio, por exemplo)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criador_id")
    @JsonIgnore // Evita loops infinitos de JSON na serialização
    private User creator;

    // Construtor vazio (exigido pelo JPA)
    public Challenge() {}

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }
}