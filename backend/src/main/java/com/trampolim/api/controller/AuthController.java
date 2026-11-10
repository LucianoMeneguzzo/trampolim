package com.trampolim.api.controller;

import com.trampolim.api.dto.AuthResponseDTO;
import com.trampolim.api.dto.LoginDTO;
import com.trampolim.api.dto.UserRegistrationDTO;
import com.trampolim.api.dto.UserResponseDTO;
import com.trampolim.api.entity.User;
import com.trampolim.api.exception.ApiException;
import com.trampolim.api.repository.UserRepository;
import com.trampolim.api.security.LimiteDeTentativas;
import com.trampolim.api.service.JwtService;
import com.trampolim.api.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final LimiteDeTentativas limite;

    // Freio contra força bruta. Os números ficam no application.properties.
    @Value("${app.limite.login.erros-por-email:5}")
    private int errosPorEmail;

    @Value("${app.limite.login.erros-por-ip:50}")
    private int errosPorIp;

    @Value("${app.limite.login.minutos:15}")
    private int minutosDeBloqueio;

    @Value("${app.limite.cadastros-por-hora:20}")
    private int cadastrosPorHora;

    public AuthController(UserService userService, UserRepository userRepository, AuthenticationManager authenticationManager,
                          JwtService jwtService, LimiteDeTentativas limite) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.limite = limite;
    }

    // 201 com o usuário criado | 400 campos inválidos | 409 e-mail já cadastrado | 429 cadastros demais do mesmo IP
    @PostMapping("/cadastro")
    public ResponseEntity<UserResponseDTO> cadastrar(@RequestBody @Valid UserRegistrationDTO dto, HttpServletRequest request) {
        // Conta toda tentativa, inclusive a que cai em "e-mail já cadastrado":
        // assim ninguém usa o cadastro para criar contas em massa nem para testar quais e-mails existem
        String chave = "cadastro|" + request.getRemoteAddr();
        limite.exigirLivre(chave, cadastrosPorHora);
        limite.registrar(chave, Duration.ofHours(1));

        User novoUsuario = userService.registerUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponseDTO(novoUsuario));
    }

    // 200 com { userId, name, type, token } | 401 e-mail ou senha errados | 429 erros demais em pouco tempo
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginDTO dto, HttpServletRequest request) {
        String email = dto.getEmail().trim();

        // Duas contagens de senha errada: por e-mail (alguém tentando adivinhar a senha de uma conta)
        // e por IP (alguém testando uma lista de e-mails). Só os erros contam: quem acerta não é barrado.
        String chaveDoIp = "login|" + request.getRemoteAddr();
        String chaveDoEmail = chaveDoIp + "|" + email.toLowerCase(Locale.ROOT);
        limite.exigirLivre(chaveDoIp, errosPorIp);
        limite.exigirLivre(chaveDoEmail, errosPorEmail);

        try {
            // 1. Autentica as credenciais com o Spring Security (compara a senha com o hash BCrypt)
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, dto.getPassword())
            );
        } catch (AuthenticationException e) {
            Duration bloqueio = Duration.ofMinutes(minutosDeBloqueio);
            limite.registrar(chaveDoIp, bloqueio);
            limite.registrar(chaveDoEmail, bloqueio);
            // Mesma mensagem para e-mail inexistente e senha errada: não revela quais e-mails existem
            throw ApiException.unauthorized("E-mail ou senha inválidos.");
        }
        limite.zerar(chaveDoEmail);

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
