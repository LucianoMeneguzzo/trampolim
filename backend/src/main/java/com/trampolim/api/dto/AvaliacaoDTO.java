package com.trampolim.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

// Corpo de POST /api/candidaturas/{id}/avaliacao
public class AvaliacaoDTO {

    @NotNull(message = "A nota é obrigatória")
    @DecimalMin(value = "0.0", message = "A nota mínima é 0")
    @DecimalMax(value = "10.0", message = "A nota máxima é 10")
    @Digits(integer = 2, fraction = 1, message = "A nota aceita no máximo uma casa decimal")
    private BigDecimal nota;

    @NotBlank(message = "O feedback é obrigatório")
    @Size(min = 20, max = 3000, message = "O feedback precisa ter entre 20 e 3000 caracteres")
    private String feedback;

    // Getters e Setters
    public BigDecimal getNota() { return nota; }
    public void setNota(BigDecimal nota) { this.nota = nota; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }
}
