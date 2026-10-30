package com.trampolim.api.service;

import com.trampolim.api.dto.UserRegistrationDTO;
import com.trampolim.api.entity.User;
import com.trampolim.api.exception.ApiException;
import com.trampolim.api.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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

        newUser.setBio(dto.getBio());
        newUser.setGithub(dto.getGithub());
        newUser.setLinkedin(dto.getLinkedin());
        newUser.setProfessionalTitle(dto.getProfessionalTitle());

        return userRepository.save(newUser);
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
