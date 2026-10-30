package com.trampolim.api.config;

import com.trampolim.api.entity.Candidatura;
import com.trampolim.api.entity.Challenge;
import com.trampolim.api.entity.User;
import com.trampolim.api.entity.UserType;
import com.trampolim.api.repository.CandidaturaRepository;
import com.trampolim.api.repository.ChallengeRepository;
import com.trampolim.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

// Quando o backend roda sem o backend/.env (banco em memória H2), cria dados para testar as telas:
// um mentor, uma aluna, um desafio e uma entrega pronta para avaliar em /business/assessment/1.
// Nunca roda no Supabase, e só roda com o banco vazio.
@Component
public class DadosDeExemplo implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosDeExemplo.class);
    private static final String SENHA = "senha-teste-123";

    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;
    private final CandidaturaRepository candidaturaRepository;
    private final PasswordEncoder passwordEncoder;
    private final String urlDoBanco;
    private final boolean ligado;

    public DadosDeExemplo(UserRepository userRepository, ChallengeRepository challengeRepository,
                          CandidaturaRepository candidaturaRepository, PasswordEncoder passwordEncoder,
                          @Value("${spring.datasource.url}") String urlDoBanco,
                          @Value("${app.dados-de-exemplo:true}") boolean ligado) {
        this.userRepository = userRepository;
        this.challengeRepository = challengeRepository;
        this.candidaturaRepository = candidaturaRepository;
        this.passwordEncoder = passwordEncoder;
        this.urlDoBanco = urlDoBanco;
        this.ligado = ligado;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!ligado || !urlDoBanco.startsWith("jdbc:h2:") || userRepository.count() > 0) {
            return;
        }

        User mentor = novoUsuario("Mentor Teste", "mentor@trampolim.local", UserType.MENTOR);
        User aluna = novoUsuario("Ana Martins", "aluna@trampolim.local", UserType.STUDENT);

        Challenge desafio = new Challenge();
        desafio.setCreator(mentor);
        desafio.setTitle("API de pedidos para uma pequena loja");
        desafio.setDescription("A loja precisa sair da planilha e registrar pedidos por uma API REST.");
        desafio.setLevel("INTERMEDIARIO");
        desafio.setTechnologies("Java, Spring Boot");
        desafio.setDeadlineDays(7);
        desafio.setSlots(20);
        desafio.setStatus("ABERTO");
        desafio.setCreatedAt(LocalDateTime.now().minusDays(3));
        challengeRepository.save(desafio);

        Candidatura entrega = new Candidatura();
        entrega.setCandidato(aluna);
        entrega.setDesafio(desafio);
        entrega.setStatus("ENVIADO");
        entrega.setDataCandidatura(LocalDateTime.now().minusDays(2));
        entrega.setDataEnvio(LocalDateTime.now().minusHours(5));
        entrega.setLinkRepositorio("https://github.com/ana-martins/api-pedidos");
        candidaturaRepository.save(entrega);

        log.warn("Dados de exemplo criados. Entre como mentor@trampolim.local / {} e abra /business/assessment/{}",
                SENHA, entrega.getId());
    }

    private User novoUsuario(String nome, String email, UserType tipo) {
        User usuario = new User();
        usuario.setName(nome);
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(SENHA));
        usuario.setType(tipo);
        return userRepository.save(usuario);
    }
}
