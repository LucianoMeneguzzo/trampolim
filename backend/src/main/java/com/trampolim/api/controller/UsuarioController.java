package com.trampolim.api.controller;

import com.trampolim.api.entity.User;
import com.trampolim.api.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UserRepository userRepository;

    public UsuarioController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Rota PROTEGIDA: só funciona se o token JWT for válido
    @GetMapping("/me")
    public ResponseEntity<User> obterMeusDados() {
        // 1. Pega o e-mail do usuário autenticado pelo Filtro JWT
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        // 2. Busca os dados completos no banco
        User usuario = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // 3. Retorna os dados (o Jackson vai ignorar a senha se configurarmos, ou podemos retornar um DTO depois)
        // Por enquanto, retornamos a entidade. A senha está criptografada, então é "seguro",
        // mas em produção o ideal é usar um UserResponseDTO sem o campo password.
        return ResponseEntity.ok(usuario);
    }
}