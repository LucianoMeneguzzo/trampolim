package com.trampolim.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

// Avisa no console quando o backend subiu sem o backend/.env e está usando o banco em memória (H2)
@Component
public class AvisoBancoEmMemoria implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AvisoBancoEmMemoria.class);

    private final String urlDoBanco;

    public AvisoBancoEmMemoria(@Value("${spring.datasource.url}") String urlDoBanco) {
        this.urlDoBanco = urlDoBanco;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (urlDoBanco.startsWith("jdbc:h2:")) {
            log.warn("Banco em memória (H2): os dados somem quando o backend parar. "
                    + "Para usar o Supabase, copie backend/.env.example para backend/.env e preencha.");
        }
    }
}
