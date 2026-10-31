package com.trampolim.api.repository;

import com.trampolim.api.entity.Avaliacao;
import com.trampolim.api.entity.Candidatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    // Verifica se uma candidatura já foi avaliada
    Optional<Avaliacao> findByCandidatura(Candidatura candidatura);

    // Busca uma avaliação pelo ID da candidatura
    Optional<Avaliacao> findByCandidaturaId(Long candidaturaId);
}