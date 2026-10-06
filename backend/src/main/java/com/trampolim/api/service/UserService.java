package com.trampolim.api.service;

import com.trampolim.api.dto.UserRegistrationDTO;
import com.trampolim.api.entity.User;
import com.trampolim.api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Injeção de dependência via construtor (padrão recomendado)
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(UserRegistrationDTO dto) {
        // 1. Verifica se o e-mail já está em uso
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Este e-mail já está cadastrado.");
        }

        // 2. Cria a entidade a partir do DTO
        User newUser = new User();
        newUser.setName(dto.getName());
        newUser.setEmail(dto.getEmail());
        // 3. Criptografa a senha antes de salvar (NUNCA salve senha pura!)
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        newUser.setType(dto.getType());

        // Campos opcionais
        newUser.setBio(dto.getBio());
        newUser.setGithub(dto.getGithub());
        newUser.setLinkedin(dto.getLinkedin());
        newUser.setProfessionalTitle(dto.getProfessionalTitle());

        // 4. Salva no banco de dados
        return userRepository.save(newUser);
    }
}