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
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    TestHelper helper;

    @BeforeEach
    void setUp() {
        helper = new TestHelper(mvc, objectMapper);
    }

    @Test
    void cadastroCriaUsuarioSemDevolverSenha() throws Exception {
        String email = emailUnico("Ana");

        mvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("name", "Ana", "email", email.toUpperCase(), "password", SENHA, "type", "STUDENT"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value(email.toLowerCase()))
                .andExpect(jsonPath("$.type").value("STUDENT"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void cadastroComEmailRepetidoRetorna409() throws Exception {
        String email = emailUnico("repetido");
        helper.cadastrar("Primeira", email, "STUDENT");

        mvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("name", "Segunda", "email", email.toUpperCase(), "password", SENHA, "type", "MENTOR"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Este e-mail já está cadastrado."));
    }

    @Test
    void cadastroComSenhaCurtaRetorna400() throws Exception {
        mvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("name", "Ana", "email", emailUnico("curta"), "password", "123", "type", "STUDENT"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.password").exists());
    }

    @Test
    void cadastroComTipoInvalidoRetorna400() throws Exception {
        mvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("name", "Ana", "email", emailUnico("tipo"), "password", SENHA, "type", "Aluno"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").exists());
    }

    @Test
    void loginDevolveTokenJwtComTipoDoUsuario() throws Exception {
        String email = emailUnico("mentor");
        helper.cadastrar("Rafael", email, "MENTOR");

        String resposta = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("email", email, "password", SENHA))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNumber())
                .andExpect(jsonPath("$.name").value("Rafael"))
                .andExpect(jsonPath("$.type").value("MENTOR"))
                .andExpect(jsonPath("$.token").value(startsWith("eyJ")))
                .andReturn().getResponse().getContentAsString();

        assertThat(helper.readJson(resposta).get("token").asText().split("\\.")).hasSize(3);
    }

    @Test
    void loginComSenhaErradaRetorna401() throws Exception {
        String email = emailUnico("errada");
        helper.cadastrar("Ana", email, "STUDENT");

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("email", email, "password", "outra-senha-qualquer"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("E-mail ou senha inválidos."));
    }

    @Test
    void loginComEmailInexistenteRetorna401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("email", emailUnico("ninguem"), "password", SENHA))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rotaProtegidaExigeTokenValido() throws Exception {
        mvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Faça login para continuar."));

        mvc.perform(get("/api/usuarios/me").header("Authorization", bearer("token.invalido.aqui")))
                .andExpect(status().isUnauthorized());

        String token = helper.novoUsuarioComToken("Bruna", "STUDENT");
        mvc.perform(get("/api/usuarios/me").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bruna"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}
