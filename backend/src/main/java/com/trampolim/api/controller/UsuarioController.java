package com.trampolim.api.controller;

import com.trampolim.api.dto.PerfilDTO;
import com.trampolim.api.dto.PerfilPublicoDTO;
import com.trampolim.api.dto.UserResponseDTO;
import com.trampolim.api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    // Edita o próprio perfil
    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> atualizarMeuPerfil(@RequestBody @Valid PerfilDTO dto) {
        return ResponseEntity.ok(new UserResponseDTO(userService.atualizarMeuPerfil(dto)));
    }

    // Rota PÚBLICA: o portfólio pode ser visto por qualquer pessoa, sem login
    @GetMapping("/{id}")
    public ResponseEntity<PerfilPublicoDTO> buscarPerfilPublico(@PathVariable Long id) {
        return ResponseEntity.ok(userService.buscarPerfilPublico(id));
    }
}
