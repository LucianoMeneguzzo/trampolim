package com.trampolim.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class EnvioDTO {

    @NotBlank(message = "O link do envio é obrigatório")
    @Pattern(regexp = "^(https?://)?(www\\.)?github\\.com/[\\w.-]+/[\\w.-]+/?$",
            message = "Use o link de um repositório do GitHub, ex: github.com/usuario/projeto")
    private String linkEnvio;

    // Getters e Setters
    public String getLinkEnvio() {
        return linkEnvio;
    }

    public void setLinkEnvio(String linkEnvio) {
        this.linkEnvio = linkEnvio;
    }
}
