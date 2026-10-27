package com.trampolim.api.dto;

import com.trampolim.api.entity.User;
import com.trampolim.api.entity.UserType;
import java.time.LocalDateTime;

// O que a API devolve sobre um usuário. Nunca inclui a senha (nem o hash).
public class UserResponseDTO {

    private Long id;
    private String name;
    private String email;
    private UserType type;
    private String bio;
    private String github;
    private String linkedin;
    private String professionalTitle;
    private LocalDateTime createdAt;

    public UserResponseDTO() {}

    public UserResponseDTO(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.type = user.getType();
        this.bio = user.getBio();
        this.github = user.getGithub();
        this.linkedin = user.getLinkedin();
        this.professionalTitle = user.getProfessionalTitle();
        this.createdAt = user.getCreatedAt();
    }

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public UserType getType() { return type; }
    public String getBio() { return bio; }
    public String getGithub() { return github; }
    public String getLinkedin() { return linkedin; }
    public String getProfessionalTitle() { return professionalTitle; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
