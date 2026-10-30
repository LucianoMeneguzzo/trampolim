package com.trampolim.api.controller;

import com.trampolim.api.dto.AuthResponseDTO;
import com.trampolim.api.dto.LoginDTO;
import com.trampolim.api.dto.UserRegistrationDTO;
import com.trampolim.api.dto.UserResponseDTO;
import com.trampolim.api.entity.User;
import com.trampolim.api.exception.ApiException;
import com.trampolim.api.repository.UserRepository;
import com.trampolim.api.service.JwtService;
import com.trampolim.api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
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

    // 201 com o usuário criado | 400 campos inválidos | 409 e-mail já cadastrado
    @PostMapping("/cadastro")
    public ResponseEntity<UserResponseDTO> cadastrar(@RequestBody @Valid UserRegistrationDTO dto) {
        User novoUsuario = userService.registerUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponseDTO(novoUsuario));
    }

    // 200 com { userId, name, type, token } | 401 e-mail ou senha errados
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginDTO dto) {
        String email = dto.getEmail().trim();

        try {
            // 1. Autentica as credenciais com o Spring Security (compara a senha com o hash BCrypt)
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, dto.getPassword())
            );
        } catch (AuthenticationException e) {
            // Mesma mensagem para e-mail inexistente e senha errada: não revela quais e-mails existem
            throw ApiException.unauthorized("E-mail ou senha inválidos.");
        }

        // 2. Busca o usuário no banco para preencher a resposta corretamente
        User usuario = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> ApiException.unauthorized("E-mail ou senha inválidos."));

        // 3. Gera o token JWT
        String token = jwtService.generateToken(usuario);

        // 4. Monta e retorna a resposta completa com os dados reais
        AuthResponseDTO resposta = new AuthResponseDTO(
                usuario.getId(),
                usuario.getName(),
                usuario.getType(),
                token
        );

        return ResponseEntity.ok(resposta);
    }
}
