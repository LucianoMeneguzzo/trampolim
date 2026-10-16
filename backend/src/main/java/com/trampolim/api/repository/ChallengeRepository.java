package com.trampolim.api.repository;

import com.trampolim.api.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    // Consulta personalizada que busca o desafio E o criador de uma só vez (evita LazyInitializationException)
    @Query("SELECT c FROM Challenge c JOIN FETCH c.creator")
    List<Challenge> findAllWithCreator();
}