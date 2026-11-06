package com.trampolim.api.dto;

import com.trampolim.api.entity.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserRegistrationDTO {

    @NotBlank(message = "O nome é obrigatório")
    @Size(max = 100, message = "O nome pode ter no máximo 100 caracteres")
    private String name;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "O e-mail deve ser válido")
    @Size(max = 150, message = "O e-mail pode ter no máximo 150 caracteres")
    private String email;

    // BCrypt só considera os primeiros 72 bytes, por isso o limite superior
    @NotBlank(message = "A senha é obrigatória")
    @Size(min = 8, max = 72, message = "A senha precisa ter entre 8 e 72 caracteres")
    private String password;

    @NotNull(message = "O tipo de usuário é obrigatório")
    private UserType type;

    // Campos opcionais
    private String bio;
    private String github;
    private String linkedin;
    private String professionalTitle;

    // Getters e Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserType getType() { return type; }
    public void setType(UserType type) { this.type = type; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getGithub() { return github; }
    public void setGithub(String github) { this.github = github; }

    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) { this.linkedin = linkedin; }

    public String getProfessionalTitle() { return professionalTitle; }
    public void setProfessionalTitle(String professionalTitle) { this.professionalTitle = professionalTitle; }
}
