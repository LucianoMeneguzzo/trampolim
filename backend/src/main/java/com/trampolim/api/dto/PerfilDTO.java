package com.trampolim.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Corpo de PUT /api/usuarios/me
public class PerfilDTO {

    // Os links viram <a href> no perfil público. Aceitar só estes domínios impede que alguém
    // cadastre um "javascript:..." e execute código no navegador de quem abrir o perfil.
    static final String REGEX_GITHUB = "^$|^(https://)?(www\\.)?github\\.com/[\\w.-]+/?$";
    static final String MENSAGEM_GITHUB = "Use o link do seu GitHub, ex: github.com/usuario";
    static final String REGEX_LINKEDIN = "^$|^(https://)?(www\\.)?linkedin\\.com/(in|company)/[\\w%.-]+/?$";
    static final String MENSAGEM_LINKEDIN = "Use o link do seu LinkedIn, ex: linkedin.com/in/usuario";

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 100, message = "O nome pode ter no máximo 100 caracteres")
    private String name;

    @Size(max = 2000, message = "A bio pode ter no máximo 2000 caracteres")
    private String bio;

    @Size(max = 150, message = "O link do GitHub pode ter no máximo 150 caracteres")
    @Pattern(regexp = REGEX_GITHUB, message = MENSAGEM_GITHUB)
    private String github;

    @Size(max = 150, message = "O link do LinkedIn pode ter no máximo 150 caracteres")
    @Pattern(regexp = REGEX_LINKEDIN, message = MENSAGEM_LINKEDIN)
    private String linkedin;

    @Size(max = 100, message = "O título profissional pode ter no máximo 100 caracteres")
    private String professionalTitle;

    // Getters e Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getGithub() { return github; }
    public void setGithub(String github) { this.github = github; }

    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) { this.linkedin = linkedin; }

    public String getProfessionalTitle() { return professionalTitle; }
    public void setProfessionalTitle(String professionalTitle) { this.professionalTitle = professionalTitle; }
}
