package com.trampolim.api.repository;

import com.trampolim.api.entity.Candidatura;
import com.trampolim.api.entity.Challenge;
import com.trampolim.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CandidaturaRepository extends JpaRepository<Candidatura, Long> {

    // Verifica se já existe uma candidatura deste usuário para este desafio específico
    @Query("SELECT c FROM Candidatura c WHERE c.candidato = :candidato AND c.desafio = :desafio")
    Optional<Candidatura> findByCandidatoAndDesafio(User candidato, Challenge desafio);

    // Busca todas as candidaturas de um desafio, trazendo os dados do candidato E do desafio
    @Query("SELECT c FROM Candidatura c JOIN FETCH c.candidato JOIN FETCH c.desafio WHERE c.desafio = :desafio")
    List<Candidatura> findAllByDesafio(Challenge desafio);

    // Inscrições de um aluno, da mais nova para a mais antiga
    @Query("SELECT c FROM Candidatura c JOIN FETCH c.candidato JOIN FETCH c.desafio WHERE c.candidato.id = :candidatoId ORDER BY c.dataCandidatura DESC")
    List<Candidatura> findAllByCandidatoId(Long candidatoId);

    long countByDesafioId(Long desafioId);

    long countByLinkEnvioIsNotNull();

    // [id do desafio, inscritos, entregas] dos desafios de um mentor
    @Query("SELECT c.desafio.id, COUNT(c), COUNT(c.linkEnvio) FROM Candidatura c WHERE c.desafio.criador.id = :mentorId GROUP BY c.desafio.id")
    List<Object[]> contarPorDesafioDoMentor(Long mentorId);

    @Query("SELECT c.dataCandidatura FROM Candidatura c WHERE c.dataCandidatura >= :desde")
    List<LocalDateTime> datasDeInscricaoDesde(LocalDateTime desde);

    // Busca uma candidatura específica pelo ID, trazendo candidato, desafio e o mentor dono do desafio
    // (para conferir permissão) e evitar LazyInitializationException
    @Query("SELECT c FROM Candidatura c JOIN FETCH c.candidato JOIN FETCH c.desafio d LEFT JOIN FETCH d.criador WHERE c.id = :id")
    Optional<Candidatura> findByIdWithDetails(Long id);
}
