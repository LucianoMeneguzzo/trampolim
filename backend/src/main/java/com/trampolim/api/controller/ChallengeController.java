package com.trampolim.api.controller;

import com.trampolim.api.dto.DesafioDTO;
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

    @PostMapping
    public ResponseEntity<DesafioRespostaDTO> criarDesafio(@RequestBody @Valid DesafioDTO dto) {
        Challenge novoDesafio = challengeService.criarDesafio(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new DesafioRespostaDTO(novoDesafio));
    }

    @GetMapping
    public ResponseEntity<List<DesafioRespostaDTO>> listarTodosDesafios() {
        List<Challenge> desafios = challengeService.listarTodosDesafios();
        List<DesafioRespostaDTO> resposta = desafios.stream()
                .map(DesafioRespostaDTO::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DesafioRespostaDTO> buscarDesafioPorId(@PathVariable Long id) {
        Challenge desafio = challengeService.buscarDesafioPorId(id);
        return ResponseEntity.ok(new DesafioRespostaDTO(desafio));
    }
}