package com.trampolim.api.dto;

import jakarta.validation.constraints.NotBlank;

public class EnvioDTO {

    @NotBlank(message = "O link do envio é obrigatório")
    private String linkEnvio;

    // Getters e Setters
    public String getLinkEnvio() {
        return linkEnvio;
    }

    public void setLinkEnvio(String linkEnvio) {
        this.linkEnvio = linkEnvio;
    }
}