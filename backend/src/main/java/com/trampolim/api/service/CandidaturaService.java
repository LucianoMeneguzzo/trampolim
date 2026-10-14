package com.trampolim.api.service;

import com.trampolim.api.dto.CandidaturaDTO;
import com.trampolim.api.entity.Candidatura;
import com.trampolim.api.entity.Challenge;
import com.trampolim.api.entity.User;
import com.trampolim.api.repository.CandidaturaRepository;
import com.trampolim.api.repository.ChallengeRepository;
import com.trampolim.api.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional // <--- ADICIONADO: Garante que todos os métodos rodem em uma única transação
public class CandidaturaService {

    private final CandidaturaRepository candidaturaRepository;
    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;

    public CandidaturaService(CandidaturaRepository candidaturaRepository, UserRepository userRepository, ChallengeRepository challengeRepository) {
        this.candidaturaRepository = candidaturaRepository;
        this.userRepository = userRepository;
        this.challengeRepository = challengeRepository;
    }

    public Candidatura aplicarParaDesafio(CandidaturaDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User candidato = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado."));

        Challenge desafio = challengeRepository.findById(dto.getDesafioId())
                .orElseThrow(() -> new RuntimeException("Desafio não encontrado."));

        Optional<Candidatura> existente = candidaturaRepository.findByCandidatoAndDesafio(candidato, desafio);
        if (existente.isPresent()) {
            throw new RuntimeException("Você já se candidatou para este desafio.");
        }

        Candidatura candidatura = new Candidatura();
        candidatura.setCandidato(candidato);
        candidatura.setDesafio(desafio);
        candidatura.setStatus("PENDENTE");
        candidatura.setDataCandidatura(LocalDateTime.now());

        return candidaturaRepository.save(candidatura);
    }

    public List<Candidatura> listarCandidaturasPorDesafio(Long desafioId) {
        Challenge desafio = challengeRepository.findById(desafioId)
                .orElseThrow(() -> new RuntimeException("Desafio não encontrado."));

        return candidaturaRepository.findAllByDesafio(desafio);
    }

    public Candidatura atualizarStatusCandidatura(Long candidaturaId, String novoStatus) {
        if (!novoStatus.equals("APROVADO") && !novoStatus.equals("REJEITADO")) {
            throw new RuntimeException("Status inválido. Use apenas 'APROVADO' ou 'REJEITADO'.");
        }

        Candidatura candidatura = candidaturaRepository.findByIdWithDetails(candidaturaId)
                .orElseThrow(() -> new RuntimeException("Candidatura não encontrada."));

        candidatura.setStatus(novoStatus);
        return candidaturaRepository.save(candidatura);
    }
}