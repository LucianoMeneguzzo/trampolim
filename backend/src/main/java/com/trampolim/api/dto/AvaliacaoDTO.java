package com.trampolim.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AvaliacaoDTO {

    @NotNull(message = "A nota é obrigatória")
    @Min(value = 0, message = "A nota deve ser no mínimo 0")
    @Max(value = 10, message = "A nota deve ser no máximo 10")
    private Integer nota;

    @NotBlank(message = "O feedback é obrigatório")
    private String feedback;

    // Getters e Setters
    public Integer getNota() {
        return nota;
    }

    public void setNota(Integer nota) {
        this.nota = nota;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}