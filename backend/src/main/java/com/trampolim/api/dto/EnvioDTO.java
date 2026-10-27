package com.trampolim.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// Corpo de PUT /api/candidaturas/{id}/envio
public class EnvioDTO {

    @NotBlank(message = "O link do repositório é obrigatório")
    @Pattern(regexp = "^(https?://)?(www\\.)?github\\.com/[\\w.-]+/[\\w.-]+/?$",
            message = "Use o link de um repositório do GitHub, ex: github.com/usuario/projeto")
    private String linkRepositorio;

    public String getLinkRepositorio() { return linkRepositorio; }
    public void setLinkRepositorio(String linkRepositorio) { this.linkRepositorio = linkRepositorio; }
}
