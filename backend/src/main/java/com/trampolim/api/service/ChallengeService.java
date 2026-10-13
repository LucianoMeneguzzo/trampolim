package com.trampolim.api.service;

import com.trampolim.api.dto.ChallengeDTO;
import com.trampolim.api.entity.Challenge;
import com.trampolim.api.repository.ChallengeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChallengeService {

    private final ChallengeRepository challengeRepository;

    // Injeção de dependência via construtor
    public ChallengeService(ChallengeRepository challengeRepository) {
        this.challengeRepository = challengeRepository;
    }

    // Método para criar um novo desafio
    public Challenge createChallenge(ChallengeDTO dto) {
        Challenge challenge = new Challenge();

        // Mapeia os dados do DTO para a Entidade
        challenge.setTitle(dto.getTitle());
        challenge.setDescription(dto.getDescription());
        challenge.setLevel(dto.getLevel());

        // Define a data de criação automaticamente
        challenge.setCreatedAt(LocalDateTime.now());

        // Salva no banco de dados e retorna o desafio criado (com o ID gerado)
        return challengeRepository.save(challenge);
    }

    // Método para listar todos os desafios (vamos usar no próximo passo)
    public List<Challenge> getAllChallenges() {
        return challengeRepository.findAll();
    }
}