package com.trampolim.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class ChallengeDTO {

    @NotBlank(message = "O título é obrigatório")
    @Size(max = 150, message = "O título pode ter no máximo 150 caracteres")
    private String title;

    @NotBlank(message = "A descrição é obrigatória")
    private String description;

    @NotBlank(message = "O nível é obrigatório")
    @Pattern(regexp = "(?i)INICIANTE|INTERMEDIARIO|AVANCADO", message = "O nível deve ser INICIANTE, INTERMEDIARIO ou AVANCADO")
    private String level;

    // Campos opcionais (tela "Publique um desafio")
    @Size(max = 10, message = "Informe no máximo 10 tecnologias")
    private List<String> technologies;

    @Size(max = 150, message = "O nome da empresa pode ter no máximo 150 caracteres")
    private String companyName;

    @Min(value = 1, message = "O prazo mínimo é 1 dia")
    @Max(value = 90, message = "O prazo máximo é 90 dias")
    private Integer deadlineDays;

    @Min(value = 1, message = "Informe pelo menos 1 vaga")
    @Max(value = 500, message = "O máximo é 500 vagas")
    private Integer slots;

    // Getters e Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public List<String> getTechnologies() { return technologies; }
    public void setTechnologies(List<String> technologies) { this.technologies = technologies; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public Integer getDeadlineDays() { return deadlineDays; }
    public void setDeadlineDays(Integer deadlineDays) { this.deadlineDays = deadlineDays; }

    public Integer getSlots() { return slots; }
    public void setSlots(Integer slots) { this.slots = slots; }
}
