package com.trampolim.api.dto;

import jakarta.validation.constraints.NotNull;

public class CandidaturaDTO {

    @NotNull(message = "O ID do desafio é obrigatório")
    private Long desafioId;

    // Getters e Setters
    public Long getDesafioId() { return desafioId; }
    public void setDesafioId(Long desafioId) { this.desafioId = desafioId; }
}