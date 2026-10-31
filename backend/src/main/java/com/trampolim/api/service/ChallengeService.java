package com.trampolim.api.service;

import com.trampolim.api.dto.DesafioDTO;
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

    public Challenge criarDesafio(DesafioDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User criador = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado."));

        Challenge challenge = new Challenge();
        challenge.setTitulo(dto.getTitulo());
        challenge.setDescricao(dto.getDescricao());
        challenge.setTecnologias(dto.getTecnologias());
        challenge.setNivel(dto.getNivel());
        challenge.setPrazo(dto.getPrazo());
        challenge.setVagas(dto.getVagas());
        challenge.setEmpresa(dto.getEmpresa());
        challenge.setDataCriacao(LocalDateTime.now());
        challenge.setCriador(criador);

        return challengeRepository.save(challenge);
    }

    public List<Challenge> listarTodosDesafios() {
        return challengeRepository.findAll();
    }

    public Challenge buscarDesafioPorId(Long id) {
        return challengeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Desafio não encontrado."));
    }
}