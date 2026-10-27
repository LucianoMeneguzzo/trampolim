package com.trampolim.api.controller;

import com.trampolim.api.dto.AvaliacaoDTO;
import com.trampolim.api.dto.AvaliacaoRespostaDTO;
import com.trampolim.api.dto.CandidaturaDTO;
import com.trampolim.api.dto.CandidaturaDetalheDTO;
import com.trampolim.api.dto.CandidaturaRespostaDTO;
import com.trampolim.api.dto.EnvioDTO;
import com.trampolim.api.entity.Candidatura;
import com.trampolim.api.service.AvaliacaoService;
import com.trampolim.api.service.CandidaturaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/candidaturas")
public class CandidaturaController {

    private final CandidaturaService candidaturaService;
    private final AvaliacaoService avaliacaoService;

    public CandidaturaController(CandidaturaService candidaturaService, AvaliacaoService avaliacaoService) {
        this.candidaturaService = candidaturaService;
        this.avaliacaoService = avaliacaoService;
    }

    // Aluno se inscreve em um desafio — só STUDENT
    @PostMapping
    public ResponseEntity<Candidatura> aplicar(@RequestBody @Valid CandidaturaDTO dto) {
        Candidatura novaCandidatura = candidaturaService.aplicarParaDesafio(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaCandidatura);
    }

    // Inscrições de um desafio — só o MENTOR dono do desafio
    @GetMapping("/desafio/{desafioId}")
    public ResponseEntity<List<CandidaturaRespostaDTO>> listarCandidaturasPorDesafio(@PathVariable Long desafioId) {
        List<Candidatura> candidaturas = candidaturaService.listarCandidaturasPorDesafio(desafioId);

        List<CandidaturaRespostaDTO> resposta = candidaturas.stream()
                .map(CandidaturaRespostaDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(resposta);
    }

    // Detalhe de uma inscrição/entrega — mentor dono do desafio ou o próprio aluno
    @GetMapping("/{id}")
    public ResponseEntity<CandidaturaDetalheDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(candidaturaService.buscarDetalhe(id));
    }

    // Endpoint para o Mentor aprovar ou rejeitar uma candidatura
    @PatchMapping("/{id}/status")
    public ResponseEntity<CandidaturaRespostaDTO> atualizarStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String novoStatus = body.get("status");
        Candidatura candidaturaAtualizada = candidaturaService.atualizarStatusCandidatura(id, novoStatus);
        return ResponseEntity.ok(new CandidaturaRespostaDTO(candidaturaAtualizada));
    }

    // Aluno envia o link do repositório — só STUDENT dono da inscrição
    @PutMapping("/{id}/envio")
    public ResponseEntity<CandidaturaDetalheDTO> enviar(@PathVariable Long id, @RequestBody @Valid EnvioDTO dto) {
        return ResponseEntity.ok(candidaturaService.enviarProjeto(id, dto));
    }

    // Mentor avalia a entrega com nota + feedback — só MENTOR dono do desafio
    @PostMapping("/{id}/avaliacao")
    public ResponseEntity<AvaliacaoRespostaDTO> avaliar(@PathVariable Long id, @RequestBody @Valid AvaliacaoDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(avaliacaoService.avaliar(id, dto));
    }
}
