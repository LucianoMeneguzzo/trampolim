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

    // Colunas novas ficam sem NOT NULL para o ddl-auto=update funcionar com desafios já gravados
    @Column(name = "tecnologias", length = 200)
    private String technologies; // Separadas por vírgula: "Java, Spring Boot, MySQL"

    @Column(name = "empresa_nome", length = 150)
    private String companyName; // Opcional: desafio publicado "em nome de" uma empresa

    @Column(name = "prazo_dias")
    private Integer deadlineDays;

    @Column(name = "vagas")
    private Integer slots;

    @Column(name = "status", length = 20)
    private String status; // "ABERTO" ou "FECHADO"

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime createdAt;

    // Relacionamento com o Usuário (mentor que criou o desafio)
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

    public String getTechnologies() { return technologies; }
    public void setTechnologies(String technologies) { this.technologies = technologies; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public Integer getDeadlineDays() { return deadlineDays; }
    public void setDeadlineDays(Integer deadlineDays) { this.deadlineDays = deadlineDays; }

    public Integer getSlots() { return slots; }
    public void setSlots(Integer slots) { this.slots = slots; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }
}
