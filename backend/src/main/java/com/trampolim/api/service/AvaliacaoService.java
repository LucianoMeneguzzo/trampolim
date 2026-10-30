package com.trampolim.api.service;

import com.trampolim.api.dto.AvaliacaoDTO;
import com.trampolim.api.dto.AvaliacaoRespostaDTO;
import com.trampolim.api.entity.Avaliacao;
import com.trampolim.api.entity.Candidatura;
import com.trampolim.api.entity.User;
import com.trampolim.api.exception.ApiException;
import com.trampolim.api.repository.AvaliacaoRepository;
import com.trampolim.api.repository.CandidaturaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AvaliacaoService {

    private final AvaliacaoRepository avaliacaoRepository;
    private final CandidaturaRepository candidaturaRepository;
    private final CandidaturaService candidaturaService;
    private final UserService userService;

    public AvaliacaoService(AvaliacaoRepository avaliacaoRepository, CandidaturaRepository candidaturaRepository,
                            CandidaturaService candidaturaService, UserService userService) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.candidaturaRepository = candidaturaRepository;
        this.candidaturaService = candidaturaService;
        this.userService = userService;
    }

    // Regras: 1) só o mentor dono do desafio avalia; 2) precisa ter projeto enviado; 3) uma avaliação por entrega
    public AvaliacaoRespostaDTO avaliar(Long candidaturaId, AvaliacaoDTO dto) {
        Candidatura candidatura = candidaturaService.buscarComDetalhes(candidaturaId);
        User mentor = userService.getAuthenticatedUser();

        CandidaturaService.exigirDonoDoDesafio(candidatura.getDesafio(), mentor);

        if (candidatura.getLinkRepositorio() == null) {
            throw ApiException.badRequest("Esta inscrição ainda não tem projeto enviado.");
        }
        if (avaliacaoRepository.existsByCandidaturaId(candidaturaId)) {
            throw ApiException.conflict("Esta entrega já foi avaliada.");
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setCandidatura(candidatura);
        avaliacao.setMentor(mentor);
        avaliacao.setNota(dto.getNota());
        avaliacao.setFeedback(dto.getFeedback().trim());
        avaliacao.setDataAvaliacao(LocalDateTime.now());

        candidatura.setStatus("AVALIADO");
        candidaturaRepository.save(candidatura);

        // TODO (Ismael): disparar o e-mail avisando o aluno
        return new AvaliacaoRespostaDTO(avaliacaoRepository.save(avaliacao));
    }
}
