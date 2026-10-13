package com.trampolim.api.service;

import com.trampolim.api.dto.ChallengeDTO;
import com.trampolim.api.entity.Challenge;
import com.trampolim.api.entity.User;
import com.trampolim.api.repository.ChallengeRepository;
import com.trampolim.api.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserRepository userRepository;

    public ChallengeService(ChallengeRepository challengeRepository, UserRepository userRepository) {
        this.challengeRepository = challengeRepository;
        this.userRepository = userRepository;
    }

    public Challenge createChallenge(ChallengeDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User creator = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado no banco de dados"));

        Challenge challenge = new Challenge();
        challenge.setTitle(dto.getTitle());
        challenge.setDescription(dto.getDescription());
        challenge.setLevel(dto.getLevel());
        challenge.setCreatedAt(LocalDateTime.now());
        challenge.setCreator(creator);

        return challengeRepository.save(challenge);
    }

    // ALTERAÇÃO AQUI: Usamos o novo método que busca junto com o criador
    public List<Challenge> getAllChallenges() {
        return challengeRepository.findAllWithCreator();
    }
}