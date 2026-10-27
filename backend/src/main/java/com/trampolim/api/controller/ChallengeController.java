package com.trampolim.api.controller;

import com.trampolim.api.dto.ChallengeDTO;
import com.trampolim.api.dto.DesafioRespostaDTO;
import com.trampolim.api.entity.Challenge;
import com.trampolim.api.service.ChallengeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/desafios")
public class ChallengeController {

    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    // Endpoint para criar um novo desafio (POST) — só MENTOR
    @PostMapping
    public ResponseEntity<DesafioRespostaDTO> criarDesafio(@RequestBody @Valid ChallengeDTO dto) {
        // 1. O serviço cria e salva a entidade
        Challenge novoDesafio = challengeService.createChallenge(dto);

        // 2. Convertemos a entidade para o DTO de resposta antes de enviar ao cliente
        DesafioRespostaDTO resposta = new DesafioRespostaDTO(novoDesafio);

        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    // Endpoint para listar todos os desafios (GET) — público
    @GetMapping
    public ResponseEntity<List<DesafioRespostaDTO>> listarDesafios() {
        // 1. O serviço busca todas as entidades no banco
        List<Challenge> desafios = challengeService.getAllChallenges();

        // 2. Convertemos a lista de entidades para uma lista de DTOs de resposta
        List<DesafioRespostaDTO> resposta = desafios.stream()
                .map(DesafioRespostaDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(resposta);
    }

    // Detalhe de um desafio (GET) — público
    @GetMapping("/{id}")
    public ResponseEntity<DesafioRespostaDTO> buscarDesafio(@PathVariable Long id) {
        return ResponseEntity.ok(new DesafioRespostaDTO(challengeService.getChallenge(id)));
    }
}
