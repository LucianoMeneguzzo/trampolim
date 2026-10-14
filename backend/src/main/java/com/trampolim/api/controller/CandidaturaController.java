package com.trampolim.api.controller;

import com.trampolim.api.dto.CandidaturaDTO;
import com.trampolim.api.dto.CandidaturaRespostaDTO;
import com.trampolim.api.entity.Candidatura;
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

    public CandidaturaController(CandidaturaService candidaturaService) {
        this.candidaturaService = candidaturaService;
    }

    @PostMapping
    public ResponseEntity<Candidatura> aplicar(@RequestBody @Valid CandidaturaDTO dto) {
        Candidatura novaCandidatura = candidaturaService.aplicarParaDesafio(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novaCandidatura);
    }

    @GetMapping("/desafio/{desafioId}")
    public ResponseEntity<List<CandidaturaRespostaDTO>> listarCandidaturasPorDesafio(@PathVariable Long desafioId) {
        List<Candidatura> candidaturas = candidaturaService.listarCandidaturasPorDesafio(desafioId);

        List<CandidaturaRespostaDTO> resposta = candidaturas.stream()
                .map(CandidaturaRespostaDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(resposta);
    }

    // NOVO: Endpoint para o Mentor aprovar ou rejeitar uma candidatura
    @PatchMapping("/{id}/status")
    public ResponseEntity<CandidaturaRespostaDTO> atualizarStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String novoStatus = body.get("status");
        Candidatura candidaturaAtualizada = candidaturaService.atualizarStatusCandidatura(id, novoStatus);
        return ResponseEntity.ok(new CandidaturaRespostaDTO(candidaturaAtualizada));
    }
}