package com.trampolim.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

// Só existe no perfil "prod" (SPRING_PROFILES_ACTIVE=prod, que a imagem Docker já define).
// Em desenvolvimento o back sobe sozinho, com banco em memória e chave sorteada. Em produção isso
// seria um risco: dados que somem e um login de exemplo com senha pública. Por isso, aqui o back
// se recusa a subir enquanto faltar configuração.
@Component
@Profile("prod")
public class ChecagemDeProducao {

    // Valor de exemplo do backend/.env.example: está no GitHub, então não serve como segredo
    static final String CHAVE_DE_EXEMPLO = "sua_chave_secreta_muito_longa_e_segura_aqui_com_pelo_menos_256_bits";

    public ChecagemDeProducao(@Value("${spring.datasource.url}") String urlDoBanco,
                              @Value("${jwt.secret}") String chaveJwt,
                              @Value("${app.cors.allowed-origins}") String origensDoFront) {
        List<String> problemas = new ArrayList<>();

        if (!urlDoBanco.startsWith("jdbc:postgresql:")) {
            problemas.add("DB_URL precisa apontar para o PostgreSQL (jdbc:postgresql://...)");
        }
        if (chaveJwt.isBlank()) {
            problemas.add("JWT_SECRET não foi definida");
        } else if (chaveJwt.length() < 32) {
            problemas.add("JWT_SECRET precisa ter pelo menos 32 caracteres");
        } else if (chaveJwt.equals(CHAVE_DE_EXEMPLO)) {
            problemas.add("JWT_SECRET está com o valor de exemplo do .env.example; gere uma chave própria");
        }
        if (origensDoFront.isBlank()) {
            problemas.add("CORS_ALLOWED_ORIGINS precisa ter o endereço do front publicado");
        }

        if (!problemas.isEmpty()) {
            throw new IllegalStateException("Configuração de produção incompleta: " + String.join("; ", problemas) + ".");
        }
    }
}
