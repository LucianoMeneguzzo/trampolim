package com.trampolim.api.config;

import com.trampolim.api.entity.Avaliacao;
import com.trampolim.api.entity.Candidatura;
import com.trampolim.api.entity.Challenge;
import com.trampolim.api.entity.User;
import com.trampolim.api.entity.UserType;
import com.trampolim.api.repository.AvaliacaoRepository;
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
    private final AvaliacaoRepository avaliacaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final String datasourceUrl;
    private final boolean ligado;

    public DataSeeder(UserRepository userRepository,
                      ChallengeRepository challengeRepository,
                      CandidaturaRepository candidaturaRepository,
                      AvaliacaoRepository avaliacaoRepository,
                      PasswordEncoder passwordEncoder,
                      @Value("${spring.datasource.url}") String datasourceUrl,
                      @Value("${app.dados-de-exemplo:true}") boolean ligado) {
        this.userRepository = userRepository;
        this.challengeRepository = challengeRepository;
        this.candidaturaRepository = candidaturaRepository;
        this.avaliacaoRepository = avaliacaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.datasourceUrl = datasourceUrl;
        this.ligado = ligado;
    }

    @Override
    public void run(String... args) {
        // Os testes automáticos criam os próprios dados
        if (!ligado) {
            return;
        }

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

        // 3. Cria 3 Desafios
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

        Challenge desafio3 = new Challenge();
        desafio3.setTitulo("Landing page para uma cafeteria");
        desafio3.setDescricao("Criar uma página responsiva que apresente o cardápio e receba pedidos pelo WhatsApp.");
        desafio3.setTecnologias("HTML, CSS, JavaScript");
        desafio3.setNivel("INICIANTE");
        desafio3.setPrazo(LocalDate.now().plusDays(10));
        desafio3.setVagas(15);
        desafio3.setEmpresa("Café Horizonte");
        desafio3.setDataCriacao(LocalDateTime.now().minusDays(20));
        desafio3.setCriador(mentor);
        challengeRepository.save(desafio3);

        // 4. Cria 1 entrega do student no desafio 1, pronta para avaliar em /business/assessment/1
        Candidatura candidatura = new Candidatura();
        candidatura.setCandidato(student);
        candidatura.setDesafio(desafio1);
        candidatura.setStatus("ENVIADO");
        candidatura.setDataCandidatura(LocalDateTime.now().minusDays(2));
        candidatura.setLinkEnvio("https://github.com/aluno-trampolim/api-rest-spring");
        candidatura.setDataEnvio(LocalDateTime.now());
        candidaturaRepository.save(candidatura);

        // 5. Cria 1 trabalho já avaliado, para o perfil público do aluno não começar vazio
        Candidatura concluida = new Candidatura();
        concluida.setCandidato(student);
        concluida.setDesafio(desafio3);
        concluida.setStatus("AVALIADO");
        concluida.setDataCandidatura(LocalDateTime.now().minusDays(18));
        concluida.setLinkEnvio("https://github.com/aluno-trampolim/landing-cafeteria");
        concluida.setRepoLinguagem("HTML");
        concluida.setRepoUltimoCommit(LocalDateTime.now().minusDays(13));
        concluida.setDataEnvio(LocalDateTime.now().minusDays(12));
        candidaturaRepository.save(concluida);

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setCandidatura(concluida);
        avaliacao.setMentor(mentor);
        avaliacao.setNota(9);
        avaliacao.setFeedback("Página clara e bem organizada, com boa atenção ao celular. Próximo passo: otimizar as imagens.");
        avaliacao.setDataAvaliacao(LocalDateTime.now().minusDays(10));
        avaliacaoRepository.save(avaliacao);

        System.out.println("✅ DataSeeder: Dados de exemplo criados com sucesso!");
        System.out.println("   👤 Mentor:  mentor@trampolim.local / senha-teste-123");
        System.out.println("   👤 Student: student@trampolim.local / senha-teste-123");
        System.out.println("   🎯 Desafios criados: 3");
        System.out.println("   📝 Entrega pronta para avaliar: /business/assessment/" + candidatura.getId());
    }
}
