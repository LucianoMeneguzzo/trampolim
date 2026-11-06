package com.trampolim.api.service;

import com.trampolim.api.dto.DesafioDTO;
import com.trampolim.api.dto.DesafioRespostaDTO;
import com.trampolim.api.entity.Challenge;
import com.trampolim.api.entity.User;
import com.trampolim.api.exception.ApiException;
import com.trampolim.api.repository.CandidaturaRepository;
import com.trampolim.api.repository.ChallengeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChallengeService {

    private final ChallengeRepository challengeRepository;
    private final CandidaturaRepository candidaturaRepository;
    private final UserService userService;

    public ChallengeService(ChallengeRepository challengeRepository, CandidaturaRepository candidaturaRepository,
                            UserService userService) {
        this.challengeRepository = challengeRepository;
        this.candidaturaRepository = candidaturaRepository;
        this.userService = userService;
    }

    // Só mentor chega aqui (SecurityConfig: POST /api/desafios -> hasRole MENTOR)
    public Challenge criarDesafio(DesafioDTO dto) {
        User criador = userService.getAuthenticatedUser();

        Challenge challenge = new Challenge();
        preencher(challenge, dto);
        challenge.setDataCriacao(LocalDateTime.now());
        challenge.setCriador(criador);

        return challengeRepository.save(challenge);
    }

    // Só o mentor que publicou edita
    public Challenge atualizarDesafio(Long id, DesafioDTO dto) {
        Challenge desafio = buscarDoMentorLogado(id);
        preencher(desafio, dto);
        challengeRepository.save(desafio);
        return desafio;
    }

    // ABERTO recebe inscrições; FECHADO não. As inscrições e avaliações existentes continuam valendo.
    public Challenge alterarStatus(Long id, String novoStatus) {
        if (!"ABERTO".equals(novoStatus) && !"FECHADO".equals(novoStatus)) {
            throw ApiException.badRequest("Status inválido. Use apenas 'ABERTO' ou 'FECHADO'.");
        }
        Challenge desafio = buscarDoMentorLogado(id);
        desafio.setStatus(novoStatus);
        challengeRepository.save(desafio);
        return desafio;
    }

    // Só apaga desafio sem inscrições: com inscrições, apagar levaria embora o portfólio de quem participou
    public void apagarDesafio(Long id) {
        Challenge desafio = buscarDoMentorLogado(id);
        if (candidaturaRepository.countByDesafioId(id) > 0) {
            throw ApiException.conflict("Este desafio já tem inscrições. Feche as inscrições em vez de apagar.");
        }
        challengeRepository.delete(desafio);
    }

    public List<DesafioRespostaDTO> listarMeusDesafios() {
        Long mentorId = userService.getAuthenticatedUser().getId();

        Map<Long, Object[]> contagens = candidaturaRepository.contarPorDesafioDoMentor(mentorId).stream()
                .collect(Collectors.toMap(linha -> (Long) linha[0], linha -> linha));

        return challengeRepository.findAllByCriadorId(mentorId).stream()
                .map(desafio -> {
                    Object[] linha = contagens.get(desafio.getId());
                    long inscritos = linha == null ? 0 : (Long) linha[1];
                    long entregas = linha == null ? 0 : (Long) linha[2];
                    return new DesafioRespostaDTO(desafio, inscritos, entregas);
                })
                .toList();
    }

    public DesafioRespostaDTO detalharDesafio(Long id) {
        return new DesafioRespostaDTO(buscarDesafioPorId(id), candidaturaRepository.countByDesafioId(id), null);
    }

    public List<Challenge> listarTodosDesafios() {
        return challengeRepository.findAllWithCreator();
    }

    public Challenge buscarDesafioPorId(Long id) {
        return challengeRepository.findByIdWithCreator(id)
                .orElseThrow(() -> ApiException.notFound("Desafio não encontrado."));
    }

    private Challenge buscarDoMentorLogado(Long id) {
        Challenge desafio = buscarDesafioPorId(id);
        User criador = desafio.getCriador();
        if (criador == null || !criador.getId().equals(userService.getAuthenticatedUser().getId())) {
            throw ApiException.forbidden("Só o mentor que publicou o desafio pode alterá-lo.");
        }
        return desafio;
    }

    private static void preencher(Challenge challenge, DesafioDTO dto) {
        challenge.setTitulo(dto.getTitulo().trim());
        challenge.setDescricao(dto.getDescricao().trim());
        challenge.setTecnologias(dto.getTecnologias().trim());
        challenge.setNivel(dto.getNivel().toUpperCase());
        challenge.setPrazo(dto.getPrazo());
        challenge.setVagas(dto.getVagas());
        challenge.setEmpresa(dto.getEmpresa().trim());
    }
}
