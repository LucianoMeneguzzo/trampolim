package com.trampolim.api.controller;

import com.trampolim.api.dto.AvaliacaoDTO;
import com.trampolim.api.dto.AvaliacaoRespostaDTO;
import com.trampolim.api.dto.CandidaturaDTO;
import com.trampolim.api.dto.CandidaturaRespostaDTO;
import com.trampolim.api.dto.EnvioDTO;
import com.trampolim.api.entity.Avaliacao;
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

    // Aluno se candidata a um desafio
    @PostMapping
    public ResponseEntity<CandidaturaRespostaDTO> aplicar(@RequestBody @Valid CandidaturaDTO dto) {
        Candidatura novaCandidatura = candidaturaService.aplicarParaDesafio(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new CandidaturaRespostaDTO(novaCandidatura));
    }

    // Mentor dono do desafio lista as candidaturas
    @GetMapping("/desafio/{desafioId}")
    public ResponseEntity<List<CandidaturaRespostaDTO>> listarCandidaturasPorDesafio(@PathVariable Long desafioId) {
        List<Candidatura> candidaturas = candidaturaService.listarCandidaturasPorDesafio(desafioId);
        List<CandidaturaRespostaDTO> resposta = candidaturas.stream()
                .map(CandidaturaRespostaDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resposta);
    }

    // Mentor dono do desafio aprova ou rejeita a candidatura
    @PatchMapping("/{id}/status")
    public ResponseEntity<CandidaturaRespostaDTO> atualizarStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String novoStatus = body.get("status");
        Candidatura candidaturaAtualizada = candidaturaService.atualizarStatusCandidatura(id, novoStatus);
        return ResponseEntity.ok(new CandidaturaRespostaDTO(candidaturaAtualizada));
    }

    // Inscrições do aluno logado
    @GetMapping("/minhas")
    public ResponseEntity<List<CandidaturaRespostaDTO>> listarMinhasCandidaturas() {
        return ResponseEntity.ok(candidaturaService.listarMinhasCandidaturas());
    }

    // Detalhe de uma candidatura, com a avaliação se já houver — mentor dono do desafio ou o próprio aluno
    @GetMapping("/{id}")
    public ResponseEntity<CandidaturaRespostaDTO> buscarCandidaturaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(candidaturaService.buscarCandidaturaPorId(id));
    }

    // Aluno envia o link do projeto
    @PutMapping("/{id}/envio")
    public ResponseEntity<CandidaturaRespostaDTO> enviarProjeto(@PathVariable Long id, @RequestBody @Valid EnvioDTO dto) {
        Candidatura candidaturaAtualizada = candidaturaService.enviarProjeto(id, dto);
        return ResponseEntity.ok(new CandidaturaRespostaDTO(candidaturaAtualizada));
    }

    // Mentor avalia a candidatura (apenas o criador do desafio pode avaliar)
    @PostMapping("/{id}/avaliacao")
    public ResponseEntity<AvaliacaoRespostaDTO> avaliarCandidatura(@PathVariable Long id, @RequestBody @Valid AvaliacaoDTO dto) {
        Avaliacao novaAvaliacao = avaliacaoService.avaliarCandidatura(id, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new AvaliacaoRespostaDTO(novaAvaliacao));
    }
}
