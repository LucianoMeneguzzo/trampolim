package com.trampolim.api.controller;

import com.trampolim.api.dto.ChallengeDTO;
import com.trampolim.api.entity.Challenge;
import com.trampolim.api.service.ChallengeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/desafios")
public class ChallengeController {

    private final ChallengeService challengeService;

    // Injeção de dependência via construtor
    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    // Endpoint para criar um novo desafio (POST)
    @PostMapping
    public ResponseEntity<Challenge> criarDesafio(@RequestBody @Valid ChallengeDTO dto) {
        Challenge novoDesafio = challengeService.createChallenge(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(novoDesafio);
    }

    // Endpoint para listar todos os desafios (GET)
    @GetMapping
    public ResponseEntity<List<Challenge>> listarDesafios() {
        List<Challenge> desafios = challengeService.getAllChallenges();
        return ResponseEntity.ok(desafios);
    }
}