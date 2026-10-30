package com.trampolim.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Atalhos para os testes: cria conta, faz login e devolve o token JWT
class TestHelper {

    static final String SENHA = "senha-forte-123";

    private final MockMvc mvc;
    private final ObjectMapper json;

    TestHelper(MockMvc mvc, ObjectMapper json) {
        this.mvc = mvc;
        this.json = json;
    }

    static String emailUnico(String prefixo) {
        return prefixo + "-" + UUID.randomUUID().toString().substring(0, 8) + "@trampolim.test";
    }

    String toJson(Object body) throws Exception {
        return json.writeValueAsString(body);
    }

    JsonNode readJson(String body) throws Exception {
        return json.readTree(body);
    }

    void cadastrar(String nome, String email, String tipo) throws Exception {
        mvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("name", nome, "email", email, "password", SENHA, "type", tipo))))
                .andExpect(status().isCreated());
    }

    String login(String email) throws Exception {
        String resposta = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of("email", email, "password", SENHA))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return readJson(resposta).get("token").asText();
    }

    String novoUsuarioComToken(String nome, String tipo) throws Exception {
        String email = emailUnico(nome.toLowerCase());
        cadastrar(nome, email, tipo);
        return login(email);
    }

    static String bearer(String token) {
        return "Bearer " + token;
    }
}
