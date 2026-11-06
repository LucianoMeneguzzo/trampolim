package com.trampolim.api.service;

import com.trampolim.api.dto.CandidaturaDTO;
import com.trampolim.api.dto.CandidaturaRespostaDTO;
import com.trampolim.api.dto.EnvioDTO;
import com.trampolim.api.entity.Avaliacao;
import com.trampolim.api.entity.Candidatura;
import com.trampolim.api.entity.Challenge;
import com.trampolim.api.entity.User;
import com.trampolim.api.exception.ApiException;
import com.trampolim.api.repository.AvaliacaoRepository;
import com.trampolim.api.repository.CandidaturaRepository;
import com.trampolim.api.repository.ChallengeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class CandidaturaService {

    private final CandidaturaRepository candidaturaRepository;
    private final ChallengeRepository challengeRepository;
    private final AvaliacaoRepository avaliacaoRepository;
    private final UserService userService;
    private final GitHubService gitHubService;

    public CandidaturaService(CandidaturaRepository candidaturaRepository, ChallengeRepository challengeRepository,
                              AvaliacaoRepository avaliacaoRepository, UserService userService, GitHubService gitHubService) {
        this.candidaturaRepository = candidaturaRepository;
        this.challengeRepository = challengeRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.userService = userService;
        this.gitHubService = gitHubService;
    }

    public Candidatura aplicarParaDesafio(CandidaturaDTO dto) {
        User candidato = userService.getAuthenticatedUser();

        Challenge desafio = challengeRepository.findById(dto.getDesafioId())
                .orElseThrow(() -> ApiException.notFound("Desafio não encontrado."));

        Optional<Candidatura> existente = candidaturaRepository.findByCandidatoAndDesafio(candidato, desafio);
        if (existente.isPresent()) {
            throw ApiException.conflict("Você já se candidatou para este desafio.");
        }

        if ("FECHADO".equals(desafio.getStatus())) {
            throw ApiException.badRequest("As inscrições deste desafio estão fechadas.");
        }
        if (desafio.getPrazo() != null && desafio.getPrazo().isBefore(LocalDate.now())) {
            throw ApiException.badRequest("O prazo deste desafio já terminou.");
        }
        if (desafio.getVagas() != null && candidaturaRepository.countByDesafioId(desafio.getId()) >= desafio.getVagas()) {
            throw ApiException.conflict("As vagas deste desafio acabaram.");
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

    // Inscrições do aluno logado, com a avaliação de cada uma quando já houver
    @Transactional(readOnly = true)
    public List<CandidaturaRespostaDTO> listarMinhasCandidaturas() {
        Long alunoId = userService.getAuthenticatedUser().getId();

        Map<Long, Avaliacao> avaliacoes = avaliacaoRepository.findAllByCandidatoId(alunoId).stream()
                .collect(Collectors.toMap(a -> a.getCandidatura().getId(), Function.identity()));

        return candidaturaRepository.findAllByCandidatoId(alunoId).stream()
                .map(c -> new CandidaturaRespostaDTO(c, avaliacoes.get(c.getId())))
                .toList();
    }

    // Aluno envia o link do repositório e o back confirma na API do GitHub que ele existe.
    // Fora de transação: a consulta externa não deve segurar uma conexão do banco enquanto espera.
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public Candidatura enviarProjeto(Long candidaturaId, EnvioDTO dto) {
        Candidatura candidatura = buscarComDetalhes(candidaturaId);
        User aluno = userService.getAuthenticatedUser();

        if (!candidatura.getCandidato().getId().equals(aluno.getId())) {
            throw ApiException.forbidden("Você só pode enviar o projeto das suas próprias candidaturas.");
        }
        if ("REJEITADO".equals(candidatura.getStatus()) || "AVALIADO".equals(candidatura.getStatus())) {
            throw ApiException.badRequest("Esta inscrição não aceita mais envios.");
        }

        String link = dto.getLinkEnvio().trim().replaceFirst("^(https?://)?(www\\.)?", "https://").replaceAll("/$", "");
        GitHubService.Repositorio repositorio = gitHubService.consultarRepositorio(link);

        candidatura.setLinkEnvio(link);
        candidatura.setRepoLinguagem(repositorio.linguagem());
        candidatura.setRepoUltimoCommit(repositorio.ultimoCommit());
        candidatura.setDataEnvio(LocalDateTime.now());
        candidatura.setStatus("ENVIADO");

        candidaturaRepository.save(candidatura);
        return candidatura;
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
