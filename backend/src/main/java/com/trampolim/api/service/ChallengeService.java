package com.trampolim.api.service;

import com.trampolim.api.dto.DesafioDTO;
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
    public Challenge criarDesafio(DesafioDTO dto) {
        User criador = userService.getAuthenticatedUser();

        Challenge challenge = new Challenge();
        challenge.setTitulo(dto.getTitulo().trim());
        challenge.setDescricao(dto.getDescricao().trim());
        challenge.setTecnologias(dto.getTecnologias().trim());
        challenge.setNivel(dto.getNivel().toUpperCase());
        challenge.setPrazo(dto.getPrazo());
        challenge.setVagas(dto.getVagas());
        challenge.setEmpresa(dto.getEmpresa().trim());
        challenge.setDataCriacao(LocalDateTime.now());
        challenge.setCriador(criador);

        return challengeRepository.save(challenge);
    }

    public List<Challenge> listarTodosDesafios() {
        return challengeRepository.findAllWithCreator();
    }

    public Challenge buscarDesafioPorId(Long id) {
        return challengeRepository.findByIdWithCreator(id)
                .orElseThrow(() -> ApiException.notFound("Desafio não encontrado."));
    }
}
