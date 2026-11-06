package com.trampolim.api.service;

import com.trampolim.api.dto.PerfilDTO;
import com.trampolim.api.dto.PerfilPublicoDTO;
import com.trampolim.api.dto.UserRegistrationDTO;
import com.trampolim.api.entity.User;
import com.trampolim.api.exception.ApiException;
import com.trampolim.api.repository.AvaliacaoRepository;
import com.trampolim.api.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AvaliacaoRepository avaliacaoRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AvaliacaoRepository avaliacaoRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.avaliacaoRepository = avaliacaoRepository;
    }

    public User registerUser(UserRegistrationDTO dto) {
        String email = dto.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("Este e-mail já está cadastrado.");
        }

        User newUser = new User();
        newUser.setName(dto.getName().trim());
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(dto.getPassword())); // grava só o hash BCrypt
        newUser.setType(dto.getType());

        newUser.setBio(textoOuNulo(dto.getBio()));
        newUser.setGithub(linkOuNulo(dto.getGithub()));
        newUser.setLinkedin(linkOuNulo(dto.getLinkedin()));
        newUser.setProfessionalTitle(textoOuNulo(dto.getProfessionalTitle()));

        return userRepository.save(newUser);
    }

    // O usuário só edita o próprio perfil: o id vem do token, nunca da URL
    public User atualizarMeuPerfil(PerfilDTO dto) {
        User usuario = getAuthenticatedUser();
        usuario.setName(dto.getName().trim());
        usuario.setBio(textoOuNulo(dto.getBio()));
        usuario.setGithub(linkOuNulo(dto.getGithub()));
        usuario.setLinkedin(linkOuNulo(dto.getLinkedin()));
        usuario.setProfessionalTitle(textoOuNulo(dto.getProfessionalTitle()));
        return userRepository.save(usuario);
    }

    // Aberto sem login: não pode incluir e-mail nem nada privado
    public PerfilPublicoDTO buscarPerfilPublico(Long id) {
        User usuario = userRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Perfil não encontrado."));
        return new PerfilPublicoDTO(usuario, avaliacaoRepository.findAllByCandidatoId(id));
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    // Os DTOs já garantem que é um link do GitHub ou LinkedIn; aqui só padroniza com https://
    private static String linkOuNulo(String link) {
        String texto = textoOuNulo(link);
        return texto == null ? null : texto.replaceFirst("^(https://)?(www\\.)?", "https://").replaceAll("/$", "");
    }

    // Usuário dono do token JWT da requisição atual (o filtro JWT já preencheu o SecurityContext)
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw ApiException.unauthorized("Faça login para continuar.");
        }
        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> ApiException.unauthorized("Usuário autenticado não encontrado."));
    }
}
