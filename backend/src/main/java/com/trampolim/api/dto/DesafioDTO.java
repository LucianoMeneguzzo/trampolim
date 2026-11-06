package com.trampolim.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class DesafioDTO {

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 255, message = "O título pode ter no máximo 255 caracteres")
    private String titulo;

    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 2000, message = "A descrição pode ter no máximo 2000 caracteres")
    private String descricao;

    @NotBlank(message = "As tecnologias são obrigatórias")
    @Size(max = 255, message = "As tecnologias podem ter no máximo 255 caracteres")
    private String tecnologias;

    @NotBlank(message = "O nível é obrigatório")
    @Pattern(regexp = "(?i)INICIANTE|INTERMEDIARIO|AVANCADO", message = "O nível deve ser INICIANTE, INTERMEDIARIO ou AVANCADO")
    private String nivel;

    @NotNull(message = "O prazo é obrigatório")
    private LocalDate prazo;

    @NotNull(message = "O número de vagas é obrigatório")
    @Positive(message = "O número de vagas deve ser maior que zero")
    private Integer vagas;

    @NotBlank(message = "O nome da empresa é obrigatório")
    @Size(max = 255, message = "O nome da empresa pode ter no máximo 255 caracteres")
    private String empresa;

    // Getters e Setters
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
}
