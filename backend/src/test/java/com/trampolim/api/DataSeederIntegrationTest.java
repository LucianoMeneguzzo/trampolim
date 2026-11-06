package com.trampolim.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static com.trampolim.api.TestHelper.bearer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Os dados de exemplo são o que o projeto mostra na primeira vez que alguém roda sem configurar nada
@SpringBootTest(properties = {
        "app.dados-de-exemplo=true",
        "spring.datasource.url=jdbc:h2:mem:seeder;MODE=PostgreSQL;DB_CLOSE_DELAY=-1"
})
@AutoConfigureMockMvc
class DataSeederIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;

    private String entrar(String email) throws Exception {
        TestHelper helper = new TestHelper(mvc, objectMapper);
        String resposta = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("email", email, "password", "senha-teste-123"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return helper.readJson(resposta).get("token").asText();
    }

    @Test
    void criaOsDadosQueAsTelasPrecisam() throws Exception {
        String mentor = entrar("mentor@trampolim.local");
        entrar("student@trampolim.local");

        mvc.perform(get("/api/desafios"))
                .andExpect(jsonPath("$.length()").value(3));

        // /business/assessment/1 abre com uma entrega pronta para avaliar
        mvc.perform(get("/api/candidaturas/1").header("Authorization", bearer(mentor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENVIADO"))
                .andExpect(jsonPath("$.linkEnvio").exists())
                .andExpect(jsonPath("$.avaliacao").isEmpty());

        // O perfil público do aluno já começa com um trabalho avaliado
        mvc.perform(get("/api/usuarios/2"))
                .andExpect(jsonPath("$.trabalhos.length()").value(1))
                .andExpect(jsonPath("$.trabalhos[0].nota").value(9));

        mvc.perform(get("/api/metricas"))
                .andExpect(jsonPath("$.inscricoes").value(2))
                .andExpect(jsonPath("$.avaliados").value(1))
                .andExpect(jsonPath("$.taxaConclusao").value(50));
    }
}
