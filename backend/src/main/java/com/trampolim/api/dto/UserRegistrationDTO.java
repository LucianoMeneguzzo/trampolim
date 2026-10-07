package com.trampolim.api.dto;

import com.trampolim.api.entity.UserType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRegistrationDTO {

    @NotBlank(message = "O nome é obrigatório")
    private String name;

    @NotBlank(message = "O e-mail é obrigatório")
    @Email(message = "O e-mail deve ser válido")
    private String email;

    @NotBlank(message = "A senha é obrigatória")
    private String password;

    @NotNull(message = "O tipo de usuário é obrigatório")
    private UserType type;

    // Campos opcionais
    private String bio;
    private String github;
    private String linkedin;
    private String professionalTitle;
}