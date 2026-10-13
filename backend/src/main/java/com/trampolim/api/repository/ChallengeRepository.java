package com.trampolim.api.repository;

import com.trampolim.api.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    // O Spring Data JPA já implementa automaticamente:
    // - save(Challenge)
    // - findById(Long)
    // - findAll()
    // - deleteById(Long)

    // Podemos adicionar métodos personalizados aqui no futuro, por exemplo:
    // List<Challenge> findByLevel(String level);
}