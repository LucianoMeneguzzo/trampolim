package com.trampolim.api.controller;

import com.trampolim.api.dto.UserRegistrationDTO;
import com.trampolim.api.entity.User;
import com.trampolim.api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    // Injeção de dependência via construtor
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<User> cadastrar(@RequestBody @Valid UserRegistrationDTO dto) {
        // Chama o serviço para registrar o usuário
        User novoUsuario = userService.registerUser(dto);

        // Retorna o status 201 (Criado) e o usuário salvo no corpo da resposta
        return ResponseEntity.status(HttpStatus.CREATED).body(novoUsuario);
    }
}