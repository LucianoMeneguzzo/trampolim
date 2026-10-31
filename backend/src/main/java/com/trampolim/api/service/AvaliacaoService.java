package com.trampolim.api.service;

import com.trampolim.api.dto.AvaliacaoDTO;
import com.trampolim.api.entity.Avaliacao;
import com.trampolim.api.entity.Candidatura;
import com.trampolim.api.entity.User;
import com.trampolim.api.repository.AvaliacaoRepository;
import com.trampolim.api.repository.CandidaturaRepository;
import com.trampolim.api.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final CandidaturaRepository candidaturaRepository;
    private final UserRepository userRepository;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository, CandidaturaRepository candidaturaRepository, UserRepository userRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.candidaturaRepository = candidaturaRepository;
        this.userRepository = userRepository;
    }

    public Avaliacao avaliarCandidatura(Long candidaturaId, AvaliacaoDTO dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User mentorLogado = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário autenticado não encontrado."));

        Candidatura candidatura = candidaturaRepository.findByIdWithDetails(candidaturaId)
                .orElseThrow(() -> new RuntimeException("Candidatura não encontrada."));

        // Regra de negócio: Apenas o mentor que publicou o desafio pode avaliar
        if (!candidatura.getDesafio().getCriador().getEmail().equals(mentorLogado.getEmail())) {
            throw new RuntimeException("Apenas o mentor criador do desafio pode avaliar esta candidatura.");
        }

        // Verifica se já foi avaliada
        if (avaliacaoRepository.findByCandidatura(candidatura).isPresent()) {
            throw new RuntimeException("Esta candidatura já foi avaliada.");
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setCandidatura(candidatura);
        avaliacao.setMentor(mentorLogado);
        avaliacao.setNota(dto.getNota());
        avaliacao.setFeedback(dto.getFeedback());
        avaliacao.setDataAvaliacao(LocalDateTime.now());

        // Atualiza o status da candidatura para refletir que foi avaliada
        candidatura.setStatus("AVALIADO");
        candidaturaRepository.save(candidatura);

        return avaliacaoRepository.save(avaliacao);
    }
}