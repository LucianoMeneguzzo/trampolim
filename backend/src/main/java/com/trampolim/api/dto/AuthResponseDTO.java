package com.trampolim.api.dto;

import com.trampolim.api.entity.UserType;

public class AuthResponseDTO {

    private Long userId;
    private String name;
    private UserType type;
    private String token;

    // Construtor vazio (necessário para o Jackson/JSON)
    public AuthResponseDTO() {}

    // Construtor com todos os argumentos
    public AuthResponseDTO(Long userId, String name, UserType type, String token) {
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.token = token;
    }

    // Getters e Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public UserType getType() { return type; }
    public void setType(UserType type) { this.type = type; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}