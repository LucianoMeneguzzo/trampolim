package com.trampolim.api.service;

import com.trampolim.api.dto.ChallengeDTO;
import com.trampolim.api.entity.Challenge;
import com.trampolim.api.entity.User;
import com.trampolim.api.exception.ApiException;
import com.trampolim.api.repository.ChallengeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final UserService userService;

    public ChallengeService(ChallengeRepository challengeRepository, UserService userService) {
        this.challengeRepository = challengeRepository;
        this.userService = userService;
    }

    // Só mentor chega aqui (SecurityConfig: POST /api/desafios -> hasRole MENTOR)
    public Challenge createChallenge(ChallengeDTO dto) {
        User creator = userService.getAuthenticatedUser();

        Challenge challenge = new Challenge();
        challenge.setTitle(dto.getTitle().trim());
        challenge.setDescription(dto.getDescription().trim());
        challenge.setLevel(dto.getLevel().toUpperCase());
        challenge.setTechnologies(juntarTecnologias(dto.getTechnologies()));
        challenge.setCompanyName(textoOuNulo(dto.getCompanyName()));
        challenge.setDeadlineDays(dto.getDeadlineDays() != null ? dto.getDeadlineDays() : 7);
        challenge.setSlots(dto.getSlots());
        challenge.setStatus("ABERTO");
        challenge.setCreatedAt(LocalDateTime.now());
        challenge.setCreator(creator);

        return challengeRepository.save(challenge);
    }

    // ALTERAÇÃO AQUI: Usamos o novo método que busca junto com o criador
    public List<Challenge> getAllChallenges() {
        return challengeRepository.findAllWithCreator();
    }

    public Challenge getChallenge(Long id) {
        return challengeRepository.findByIdWithCreator(id)
                .orElseThrow(() -> ApiException.notFound("Desafio não encontrado."));
    }

    private static String juntarTecnologias(List<String> tecnologias) {
        if (tecnologias == null) return null;
        String juntas = String.join(", ", tecnologias.stream().map(String::trim).filter(t -> !t.isEmpty()).toList());
        return juntas.isEmpty() ? null : juntas;
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
