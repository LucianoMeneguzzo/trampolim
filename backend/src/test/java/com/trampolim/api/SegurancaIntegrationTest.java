package com.trampolim.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static com.trampolim.api.TestHelper.SENHA;
import static com.trampolim.api.TestHelper.bearer;
import static com.trampolim.api.TestHelper.emailUnico;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Limite de tentativas no login, cabeçalhos de segurança e o que o actuator expõe
@SpringBootTest
@AutoConfigureMockMvc
class SegurancaIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    TestHelper helper;

    @BeforeEach
    void setUp() {
        helper = new TestHelper(mvc, objectMapper);
    }

    private void tentarLogin(String email, String senha, int statusEsperado) throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("email", email, "password", senha))))
                .andExpect(status().is(statusEsperado));
    }

    @Test
    void cincoSenhasErradasBloqueiamOLoginDaqueleEmail() throws Exception {
        String email = emailUnico("alvo");
        String outroEmail = emailUnico("vizinha");
        helper.cadastrar("Ana", email, "STUDENT");
        helper.cadastrar("Bia", outroEmail, "STUDENT");

        for (int i = 0; i < 5; i++) {
            tentarLogin(email, "senha-errada-" + i, 401);
        }

        // Bloqueado: nem a senha certa entra, e a resposta explica o motivo
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("email", email.toUpperCase(), "password", SENHA))))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.erro").value("Muitas tentativas. Aguarde alguns minutos e tente de novo."));

        // Quem não errou continua entrando normalmente
        tentarLogin(outroEmail, SENHA, 200);
    }

    @Test
    void acertarASenhaZeraAContagemDeErros() throws Exception {
        String email = emailUnico("distraida");
        helper.cadastrar("Carla", email, "STUDENT");

        for (int i = 0; i < 4; i++) {
            tentarLogin(email, "senha-errada-" + i, 401);
        }
        tentarLogin(email, SENHA, 200);

        // Se a contagem não tivesse zerado, o primeiro erro daqui já bloquearia
        for (int i = 0; i < 4; i++) {
            tentarLogin(email, "senha-errada-" + i, 401);
        }
        tentarLogin(email, SENHA, 200);
    }

    @Test
    void loginRecusaCamposGrandesDemais() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("email", emailUnico("longa"), "password", "x".repeat(73)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());
    }

    @Test
    void respostasLevamOsCabecalhosDeSeguranca() throws Exception {
        mvc.perform(get("/api/desafios"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("X-Frame-Options", "DENY"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'"))
                .andExpect(header().string("Permissions-Policy", "camera=(), microphone=(), geolocation=()"));
    }

    @Test
    void actuatorSoMostraQueOBackEstaDePe() throws Exception {
        // Público e sem detalhes: nada de versão do banco, espaço em disco etc.
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());

        // O resto do actuator não é público, e nem logado existe
        mvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());

        String token = helper.novoUsuarioComToken("Dani", "MENTOR");
        mvc.perform(get("/actuator/env").header("Authorization", bearer(token))).andExpect(status().isNotFound());
        mvc.perform(get("/actuator/beans").header("Authorization", bearer(token))).andExpect(status().isNotFound());
    }
}
