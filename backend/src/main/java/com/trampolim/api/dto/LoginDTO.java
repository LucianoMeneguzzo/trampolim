package com.trampolim.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginDTO {

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "O e-mail deve ser válido")
    @Size(max = 150, message = "O e-mail pode ter no máximo 150 caracteres")
    private String email;

    // Mesmo teto do cadastro: nenhuma senha válida passa de 72 caracteres
    @NotBlank(message = "A senha é obrigatória")
    @Size(max = 72, message = "A senha pode ter no máximo 72 caracteres")
    private String password;

    // Getters e Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
