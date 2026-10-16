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

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(UserRegistrationDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Este e-mail já está cadastrado.");
        }

        User newUser = new User();
        newUser.setName(dto.getName());
        newUser.setEmail(dto.getEmail());
        newUser.setPassword(passwordEncoder.encode(dto.getPassword()));
        newUser.setType(dto.getType());

        newUser.setBio(dto.getBio());
        newUser.setGithub(dto.getGithub());
        newUser.setLinkedin(dto.getLinkedin());
        newUser.setProfessionalTitle(dto.getProfessionalTitle());

        return userRepository.save(newUser);
    }
}