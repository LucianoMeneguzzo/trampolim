package com.trampolim.api.controller;

import com.trampolim.api.dto.UserResponseDTO;
import com.trampolim.api.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UserService userService;

    public UsuarioController(UserService userService) {
        this.userService = userService;
    }

    // Rota PROTEGIDA: só funciona se o token JWT for válido (sem token -> 401)
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> obterMeusDados() {
        return ResponseEntity.ok(new UserResponseDTO(userService.getAuthenticatedUser()));
    }
}
