package com.trampolim.api.repository;

import com.trampolim.api.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    // Busca todos os desafios trazendo o criador de uma vez (evita LazyInitializationException)
    @Query("SELECT c FROM Challenge c JOIN FETCH c.criador")
    List<Challenge> findAllWithCreator();

    // Busca um desafio específico pelo ID, trazendo o criador
    @Query("SELECT c FROM Challenge c JOIN FETCH c.criador WHERE c.id = :id")
    Optional<Challenge> findByIdWithCreator(Long id);
}