package com.trampolim.api.dto;

import com.trampolim.api.entity.Challenge;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DesafioRespostaDTO {

    private Long id;
    private String titulo;
    private String descricao;
    private String tecnologias;
    private String nivel;
    private LocalDate prazo;
    private Integer vagas;
    private String empresa;
    private LocalDateTime dataCriacao;
    private String nomeCriador;
    private Long criadorId;
    private String status;
    private Long inscritos; // só vem no detalhe e em "meus desafios"
    private Long entregas;  // só vem em "meus desafios"

    public DesafioRespostaDTO() {}

    public DesafioRespostaDTO(Challenge desafio) {
        this.id = desafio.getId();
        this.titulo = desafio.getTitulo();
        this.descricao = desafio.getDescricao();
        this.tecnologias = desafio.getTecnologias();
        this.nivel = desafio.getNivel();
        this.prazo = desafio.getPrazo();
        this.vagas = desafio.getVagas();
        this.empresa = desafio.getEmpresa();
        this.dataCriacao = desafio.getDataCriacao();

        this.status = desafio.getStatus();

        if (desafio.getCriador() != null) {
            this.nomeCriador = desafio.getCriador().getName();
            this.criadorId = desafio.getCriador().getId();
        }
    }

    public DesafioRespostaDTO(Challenge desafio, long inscritos, Long entregas) {
        this(desafio);
        this.inscritos = inscritos;
        this.entregas = entregas;
    }

    // Getters
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getTecnologias() { return tecnologias; }
    public String getNivel() { return nivel; }
    public LocalDate getPrazo() { return prazo; }
    public Integer getVagas() { return vagas; }
    public String getEmpresa() { return empresa; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public String getNomeCriador() { return nomeCriador; }
    public Long getCriadorId() { return criadorId; }
    public String getStatus() { return status; }
    public Long getInscritos() { return inscritos; }
    public Long getEntregas() { return entregas; }
}