package com.trampolim.api.dto;

import com.trampolim.api.entity.Challenge;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class DesafioRespostaDTO {

    private Long id;
    private String titulo;
    private String descricao;
    private String nivel;
    private List<String> tecnologias;
    private String empresaNome;
    private Integer prazoDias;
    private Integer vagas;
    private String status;
    private LocalDateTime dataCriacao;
    private String nomeCriador;

    // Construtor vazio (exigido por algumas ferramentas de serialização)
    public DesafioRespostaDTO() {}

    // Construtor inteligente: recebe a entidade Challenge e mapeia apenas o que queremos mostrar
    public DesafioRespostaDTO(Challenge desafio) {
        this.id = desafio.getId();
        this.titulo = desafio.getTitle();
        this.descricao = desafio.getDescription();
        this.nivel = desafio.getLevel();
        this.tecnologias = separarTecnologias(desafio.getTechnologies());
        this.empresaNome = desafio.getCompanyName();
        this.prazoDias = desafio.getDeadlineDays();
        this.vagas = desafio.getSlots();
        this.status = desafio.getStatus() != null ? desafio.getStatus() : "ABERTO";
        this.dataCriacao = desafio.getCreatedAt();

        // Verifica se existe um criador vinculado para evitar erro de valor nulo
        if (desafio.getCreator() != null) {
            this.nomeCriador = desafio.getCreator().getName();
        } else {
            this.nomeCriador = "Desconhecido";
        }
    }

    private static List<String> separarTecnologias(String tecnologias) {
        if (tecnologias == null || tecnologias.isBlank()) return List.of();
        return Arrays.stream(tecnologias.split(",")).map(String::trim).filter(t -> !t.isEmpty()).toList();
    }

    // Getters (não precisamos de setters, pois este objeto é apenas para resposta)
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getNivel() { return nivel; }
    public List<String> getTecnologias() { return tecnologias; }
    public String getEmpresaNome() { return empresaNome; }
    public Integer getPrazoDias() { return prazoDias; }
    public Integer getVagas() { return vagas; }
    public String getStatus() { return status; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public String getNomeCriador() { return nomeCriador; }
}
