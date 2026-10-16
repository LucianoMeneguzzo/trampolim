package com.trampolim.api.controller;

import com.trampolim.api.dto.AuthResponseDTO;
import com.trampolim.api.dto.LoginDTO;
import com.trampolim.api.dto.UserRegistrationDTO;
import com.trampolim.api.entity.User;
import com.trampolim.api.repository.UserRepository;
import com.trampolim.api.service.JwtService;
import com.trampolim.api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(UserService userService, UserRepository userRepository, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<User> cadastrar(@RequestBody @Valid UserRegistrationDTO dto) {
        User novoUsuario = userService.registerUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoUsuario);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginDTO dto) {
        try {
            // 1. Autentica as credenciais com o Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
            );

            // 2. Busca o usuário no banco para preencher a resposta corretamente
            User usuario = userRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            // 3. Gera o token JWT
            String token = jwtService.generateToken(usuario.getEmail());

            // 4. Monta e retorna a resposta completa com os dados reais
            AuthResponseDTO resposta = new AuthResponseDTO(
                    usuario.getId(),
                    usuario.getName(),
                    usuario.getType(),
                    token
            );

            return ResponseEntity.ok(resposta);

        } catch (Exception e) {
            // Retorna 401 (Não Autorizado) se a senha estiver errada ou usuário não existir
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}