package com.trampolim.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static com.trampolim.api.TestHelper.SENHA;
import static com.trampolim.api.TestHelper.emailUnico;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Sobe o back com limites baixos (e banco próprio) para testar o freio por IP sem criar dezenas de contas
@SpringBootTest(properties = {
        "app.limite.cadastros-por-hora=3",
        "app.limite.login.erros-por-ip=4",
        "spring.datasource.url=jdbc:h2:mem:limites;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
class LimiteDeCadastroIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    private String corpo(Map<String, String> campos) throws Exception {
        return objectMapper.writeValueAsString(campos);
    }

    @Test
    void mesmoIpNaoCriaContasSemParar() throws Exception {
        String repetido = emailUnico("repetido");

        // As três tentativas permitidas: duas contas novas e uma com e-mail repetido (que também conta)
        mvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(Map.of("name", "Um", "email", repetido, "password", SENHA, "type", "STUDENT"))))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(Map.of("name", "Dois", "email", emailUnico("dois"), "password", SENHA, "type", "STUDENT"))))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(Map.of("name", "Três", "email", repetido, "password", SENHA, "type", "STUDENT"))))
                .andExpect(status().isConflict());

        mvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(Map.of("name", "Quatro", "email", emailUnico("quatro"), "password", SENHA, "type", "STUDENT"))))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.erro").value("Muitas tentativas. Aguarde alguns minutos e tente de novo."));

        // Outro IP não é afetado
        mvc.perform(post("/api/auth/cadastro").contentType(MediaType.APPLICATION_JSON)
                        .with(request -> { request.setRemoteAddr("203.0.113.7"); return request; })
                        .content(corpo(Map.of("name", "Cinco", "email", emailUnico("cinco"), "password", SENHA, "type", "STUDENT"))))
                .andExpect(status().isCreated());
    }

    @Test
    void mesmoIpNaoTestaUmaListaDeEmails() throws Exception {
        // Quatro erros com e-mails diferentes, vindos do mesmo IP
        for (int i = 0; i < 4; i++) {
            mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .with(request -> { request.setRemoteAddr("198.51.100.9"); return request; })
                            .content(corpo(Map.of("email", emailUnico("lista"), "password", "senha-qualquer"))))
                    .andExpect(status().isUnauthorized());
        }

        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .with(request -> { request.setRemoteAddr("198.51.100.9"); return request; })
                        .content(corpo(Map.of("email", emailUnico("lista"), "password", "senha-qualquer"))))
                .andExpect(status().isTooManyRequests());

        // De outro IP, a resposta volta a ser o 401 normal
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .with(request -> { request.setRemoteAddr("198.51.100.10"); return request; })
                        .content(corpo(Map.of("email", emailUnico("lista"), "password", "senha-qualquer"))))
                .andExpect(status().isUnauthorized());
    }
}
