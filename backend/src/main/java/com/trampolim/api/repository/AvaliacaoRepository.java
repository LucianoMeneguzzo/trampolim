package com.trampolim.api.repository;

import com.trampolim.api.entity.Avaliacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    // Traz o mentor junto para montar a resposta sem LazyInitializationException
    @Query("SELECT a FROM Avaliacao a JOIN FETCH a.mentor WHERE a.candidatura.id = :candidaturaId")
    Optional<Avaliacao> findByCandidaturaId(Long candidaturaId);

    boolean existsByCandidaturaId(Long candidaturaId);
}
