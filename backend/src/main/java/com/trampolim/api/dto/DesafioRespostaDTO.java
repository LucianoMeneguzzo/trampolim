package com.trampolim.api.dto;

import com.trampolim.api.entity.Challenge;
import java.time.LocalDateTime;

public class DesafioRespostaDTO {

    private Long id;
    private String titulo;
    private String descricao;
    private String nivel;
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
        this.dataCriacao = desafio.getCreatedAt();

        // Verifica se existe um criador vinculado para evitar erro de valor nulo
        if (desafio.getCreator() != null) {
            this.nomeCriador = desafio.getCreator().getName();
        } else {
            this.nomeCriador = "Desconhecido";
        }
    }

    // Getters (não precisamos de setters, pois este objeto é apenas para resposta)
    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getNivel() { return nivel; }
    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public String getNomeCriador() { return nomeCriador; }
}