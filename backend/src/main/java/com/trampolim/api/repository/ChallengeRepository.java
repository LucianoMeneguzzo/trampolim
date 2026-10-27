package com.trampolim.api.repository;

import com.trampolim.api.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    // Consulta personalizada que busca o desafio E o criador de uma só vez (evita LazyInitializationException).
    // LEFT JOIN para não sumir com desafios antigos que não têm criador.
    @Query("SELECT c FROM Challenge c LEFT JOIN FETCH c.creator ORDER BY c.createdAt DESC")
    List<Challenge> findAllWithCreator();

    @Query("SELECT c FROM Challenge c LEFT JOIN FETCH c.creator WHERE c.id = :id")
    Optional<Challenge> findByIdWithCreator(Long id);
}
