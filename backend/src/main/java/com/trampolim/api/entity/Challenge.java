package com.trampolim.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "desafios")
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false, length = 2000)
    private String descricao;

    @Column(nullable = false)
    private String tecnologias;

    @Column(nullable = false)
    private String nivel;

    @Column(nullable = false)
    private LocalDate prazo;

    @Column(nullable = false)
    private Integer vagas;

    @Column(nullable = false)
    private String empresa;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(length = 20)
    private String status; // ABERTO ou FECHADO (desafios antigos, sem valor, contam como ABERTO)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criador_id", nullable = false)
    @JsonIgnore
    private User criador;

    public Challenge() {
        this.dataCriacao = LocalDateTime.now();
        this.status = "ABERTO";
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getTecnologias() { return tecnologias; }
    public void setTecnologias(String tecnologias) { this.tecnologias = tecnologias; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public LocalDate getPrazo() { return prazo; }
    public void setPrazo(LocalDate prazo) { this.prazo = prazo; }

    public Integer getVagas() { return vagas; }
    public void setVagas(Integer vagas) { this.vagas = vagas; }

    public String getEmpresa() { return empresa; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public String getStatus() { return status == null ? "ABERTO" : status; }
    public void setStatus(String status) { this.status = status; }

    public User getCriador() { return criador; }
    public void setCriador(User criador) { this.criador = criador; }
}