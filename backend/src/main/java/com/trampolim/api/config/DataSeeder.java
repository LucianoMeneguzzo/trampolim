package com.trampolim.api.config;

import com.trampolim.api.entity.Candidatura;
import com.trampolim.api.entity.Challenge;
import com.trampolim.api.entity.User;
import com.trampolim.api.entity.UserType;
import com.trampolim.api.repository.CandidaturaRepository;
import com.trampolim.api.repository.ChallengeRepository;
import com.trampolim.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final CandidaturaRepository candidaturaRepository;
    private final PasswordEncoder passwordEncoder;
    private final String datasourceUrl;

    public DataSeeder(UserRepository userRepository,
                      ChallengeRepository challengeRepository,
                      CandidaturaRepository candidaturaRepository,
                      PasswordEncoder passwordEncoder,
                      @Value("${spring.datasource.url}") String datasourceUrl) {
        this.userRepository = userRepository;
        this.challengeRepository = challengeRepository;
        this.candidaturaRepository = candidaturaRepository;
        this.passwordEncoder = passwordEncoder;
        this.datasourceUrl = datasourceUrl;
    }

    @Override
    public void run(String... args) {
        // Só popula o banco se estiver usando H2 (sem .env)
        if (!datasourceUrl.contains("h2")) {
            System.out.println("🔒 DataSeeder: Banco PostgreSQL detectado. Dados de exemplo NÃO serão inseridos.");
            return;
        }

        // Evita duplicar dados em caso de restart
        if (userRepository.count() > 0) {
            System.out.println("🔒 DataSeeder: Banco H2 já possui dados. Seed ignorado.");
            return;
        }

        System.out.println("🌱 DataSeeder: Populando banco H2 com dados de exemplo...");

        // 1. Cria o Mentor
        User mentor = new User();
        mentor.setName("Mentor Trampolim");
        mentor.setEmail("mentor@trampolim.local");
        mentor.setPassword(passwordEncoder.encode("senha-teste-123"));
        mentor.setType(UserType.MENTOR); // Usando o Enum
        mentor.setBio("Mentor sênior com 10 anos de experiência em desenvolvimento.");
        mentor.setProfessionalTitle("Arquiteto de Software"); // Nome correto do setter
        mentor.setGithub("https://github.com/mentor-trampolim");
        mentor.setLinkedin("https://linkedin.com/in/mentor-trampolim");
        userRepository.save(mentor);

        // 2. Cria o Student
        User student = new User();
        student.setName("Aluno Trampolim");
        student.setEmail("student@trampolim.local");
        student.setPassword(passwordEncoder.encode("senha-teste-123"));
        student.setType(UserType.STUDENT); // Usando o Enum
        student.setBio("Estudante apaixonado por tecnologia em busca de desafios reais.");
        student.setProfessionalTitle("Desenvolvedor Júnior"); // Nome correto do setter
        student.setGithub("https://github.com/aluno-trampolim");
        student.setLinkedin("https://linkedin.com/in/aluno-trampolim");
        userRepository.save(student);

        // 3. Cria 2 Desafios
        Challenge desafio1 = new Challenge();
        desafio1.setTitulo("API REST com Spring Boot");
        desafio1.setDescricao("Desenvolver uma API REST completa com autenticação JWT, CRUD de usuários e testes unitários.");
        desafio1.setTecnologias("Java, Spring Boot, PostgreSQL, JWT");
        desafio1.setNivel("INTERMEDIARIO");
        desafio1.setPrazo(LocalDate.now().plusDays(30));
        desafio1.setVagas(5);
        desafio1.setEmpresa("TechCorp");
        desafio1.setDataCriacao(LocalDateTime.now());
        desafio1.setCriador(mentor);
        challengeRepository.save(desafio1);

        Challenge desafio2 = new Challenge();
        desafio2.setTitulo("Frontend com React e TypeScript");
        desafio2.setDescricao("Construir uma aplicação React com TypeScript, gerenciamento de estado e integração com API.");
        desafio2.setTecnologias("React, TypeScript, Vite, Tailwind CSS");
        desafio2.setNivel("INICIANTE");
        desafio2.setPrazo(LocalDate.now().plusDays(15));
        desafio2.setVagas(10);
        desafio2.setEmpresa("StartupXYZ");
        desafio2.setDataCriacao(LocalDateTime.now());
        desafio2.setCriador(mentor);
        challengeRepository.save(desafio2);

        // 4. Cria 1 Candidatura do student no desafio 1
        Candidatura candidatura = new Candidatura();
        candidatura.setCandidato(student);
        candidatura.setDesafio(desafio1);
        candidatura.setStatus("PENDENTE");
        candidatura.setDataCandidatura(LocalDateTime.now());
        candidaturaRepository.save(candidatura);

        System.out.println("✅ DataSeeder: Dados de exemplo criados com sucesso!");
        System.out.println("   👤 Mentor:  mentor@trampolim.local / senha-teste-123");
        System.out.println("   👤 Student: student@trampolim.local / senha-teste-123");
        System.out.println("   🎯 Desafios criados: 2");
        System.out.println("   📝 Candidaturas criadas: 1");
    }
}