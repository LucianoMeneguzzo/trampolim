package com.trampolim.api.service;

import com.trampolim.api.dto.CandidaturaDTO;
import com.trampolim.api.dto.CandidaturaRespostaDTO;
import com.trampolim.api.dto.EnvioDTO;
import com.trampolim.api.entity.Candidatura;
import com.trampolim.api.entity.Challenge;
import com.trampolim.api.entity.User;
import com.trampolim.api.exception.ApiException;
import com.trampolim.api.repository.AvaliacaoRepository;
import com.trampolim.api.repository.CandidaturaRepository;
import com.trampolim.api.repository.ChallengeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CandidaturaService {

    private final CandidaturaRepository candidaturaRepository;
    private final ChallengeRepository challengeRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final UserService userService;

    public CandidaturaService(CandidaturaRepository candidaturaRepository, ChallengeRepository challengeRepository,
                              AvaliacaoRepository avaliacaoRepository, UserService userService) {
        this.candidaturaRepository = candidaturaRepository;
        this.challengeRepository = challengeRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.userService = userService;
    }

    public Candidatura aplicarParaDesafio(CandidaturaDTO dto) {
        User candidato = userService.getAuthenticatedUser();

        Challenge desafio = challengeRepository.findById(dto.getDesafioId())
                .orElseThrow(() -> ApiException.notFound("Desafio não encontrado."));

        Optional<Candidatura> existente = candidaturaRepository.findByCandidatoAndDesafio(candidato, desafio);
        if (existente.isPresent()) {
            throw ApiException.conflict("Você já se candidatou para este desafio.");
        }

        Candidatura candidatura = new Candidatura();
        candidatura.setCandidato(candidato);
        candidatura.setDesafio(desafio);
        candidatura.setStatus("PENDENTE");
        candidatura.setDataCandidatura(LocalDateTime.now());

        return candidaturaRepository.save(candidatura);
    }

    // Só o mentor que publicou o desafio vê quem se inscreveu
    public List<Candidatura> listarCandidaturasPorDesafio(Long desafioId) {
        Challenge desafio = challengeRepository.findByIdWithCreator(desafioId)
                .orElseThrow(() -> ApiException.notFound("Desafio não encontrado."));

        exigirDonoDoDesafio(desafio, userService.getAuthenticatedUser());

        return candidaturaRepository.findAllByDesafio(desafio);
    }

    public Candidatura atualizarStatusCandidatura(Long candidaturaId, String novoStatus) {
        if (!"APROVADO".equals(novoStatus) && !"REJEITADO".equals(novoStatus)) {
            throw ApiException.badRequest("Status inválido. Use apenas 'APROVADO' ou 'REJEITADO'.");
        }

        Candidatura candidatura = buscarComDetalhes(candidaturaId);
        exigirDonoDoDesafio(candidatura.getDesafio(), userService.getAuthenticatedUser());

        candidatura.setStatus(novoStatus);
        return candidaturaRepository.save(candidatura);
    }

    // Aluno envia o link do repositório. A validação na API do GitHub (linguagem, último commit) entra aqui depois.
    public Candidatura enviarProjeto(Long candidaturaId, EnvioDTO dto) {
        Candidatura candidatura = buscarComDetalhes(candidaturaId);
        User aluno = userService.getAuthenticatedUser();

        if (!candidatura.getCandidato().getId().equals(aluno.getId())) {
            throw ApiException.forbidden("Você só pode enviar o projeto das suas próprias candidaturas.");
        }
        if ("REJEITADO".equals(candidatura.getStatus()) || "AVALIADO".equals(candidatura.getStatus())) {
            throw ApiException.badRequest("Esta inscrição não aceita mais envios.");
        }

        String link = dto.getLinkEnvio().trim().replaceFirst("^(https?://)?(www\\.)?", "https://");
        candidatura.setLinkEnvio(link.replaceAll("/$", ""));
        candidatura.setDataEnvio(LocalDateTime.now());
        candidatura.setStatus("ENVIADO");

        return candidaturaRepository.save(candidatura);
    }

    // Quem pode ver: o mentor dono do desafio ou o próprio aluno inscrito
    @Transactional(readOnly = true)
    public CandidaturaRespostaDTO buscarCandidaturaPorId(Long candidaturaId) {
        Candidatura candidatura = buscarComDetalhes(candidaturaId);
        User usuario = userService.getAuthenticatedUser();

        boolean ehCandidato = candidatura.getCandidato().getId().equals(usuario.getId());
        if (!ehCandidato) {
            exigirDonoDoDesafio(candidatura.getDesafio(), usuario);
        }

        return new CandidaturaRespostaDTO(candidatura, avaliacaoRepository.findByCandidaturaId(candidaturaId).orElse(null));
    }

    Candidatura buscarComDetalhes(Long candidaturaId) {
        return candidaturaRepository.findByIdWithDetails(candidaturaId)
                .orElseThrow(() -> ApiException.notFound("Candidatura não encontrada."));
    }

    static void exigirDonoDoDesafio(Challenge desafio, User usuario) {
        User criador = desafio.getCriador();
        if (criador == null || !criador.getId().equals(usuario.getId())) {
            throw ApiException.forbidden("Só o mentor que publicou o desafio pode acessar estas inscrições.");
        }
    }
}
