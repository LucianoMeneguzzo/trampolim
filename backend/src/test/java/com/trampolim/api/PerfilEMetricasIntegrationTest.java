package com.trampolim.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trampolim.api.TestHelper.Conta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

import static com.trampolim.api.TestHelper.bearer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Perfil público (portfólio), edição do próprio perfil, "minhas candidaturas" e painel de métricas
@SpringBootTest
@AutoConfigureMockMvc
class PerfilEMetricasIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    TestHelper helper;

    @BeforeEach
    void setUp() {
        helper = new TestHelper(mvc, objectMapper);
    }

    private long criarDesafio(String tokenMentor) throws Exception {
        String resposta = mvc.perform(post("/api/desafios")
                        .header("Authorization", bearer(tokenMentor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of(
                                "titulo", "API de pedidos",
                                "descricao", "Uma loja precisa sair da planilha.",
                                "nivel", "INICIANTE",
                                "tecnologias", "Java, Spring Boot",
                                "empresa", "Loja Horizonte",
                                "prazo", "2030-12-31",
                                "vagas", 5))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return helper.readJson(resposta).get("id").asLong();
    }

    private long entregar(String tokenAluno, long desafioId) throws Exception {
        String resposta = mvc.perform(post("/api/candidaturas")
                        .header("Authorization", bearer(tokenAluno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("desafioId", desafioId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = helper.readJson(resposta).get("id").asLong();

        mvc.perform(put("/api/candidaturas/" + id + "/envio")
                        .header("Authorization", bearer(tokenAluno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("linkEnvio", "github.com/ana/api-pedidos"))))
                .andExpect(status().isOk());
        return id;
    }

    private void avaliar(String tokenMentor, long candidaturaId, int nota) throws Exception {
        mvc.perform(post("/api/candidaturas/" + candidaturaId + "/avaliacao")
                        .header("Authorization", bearer(tokenMentor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("nota", nota, "feedback", "A estrutura da API está clara."))))
                .andExpect(status().isCreated());
    }

    @Test
    void perfilPublicoAbreSemLoginEMostraOTrabalhoAvaliadoSemOEmail() throws Exception {
        String mentor = helper.novoUsuarioComToken("Rafael", "MENTOR");
        Conta aluna = helper.novaConta("Ana", "STUDENT");

        long candidatura = entregar(aluna.token(), criarDesafio(mentor));

        // Antes da avaliação ainda não é portfólio
        mvc.perform(get("/api/usuarios/" + aluna.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana"))
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.trabalhos.length()").value(0));

        avaliar(mentor, candidatura, 9);

        mvc.perform(get("/api/usuarios/" + aluna.id()))
                .andExpect(jsonPath("$.trabalhos.length()").value(1))
                .andExpect(jsonPath("$.trabalhos[0].tituloDesafio").value("API de pedidos"))
                .andExpect(jsonPath("$.trabalhos[0].tecnologias").value("Java, Spring Boot"))
                .andExpect(jsonPath("$.trabalhos[0].nomeMentor").value("Rafael"))
                .andExpect(jsonPath("$.trabalhos[0].nota").value(9))
                .andExpect(jsonPath("$.trabalhos[0].linkEnvio").value("https://github.com/ana/api-pedidos"));

        mvc.perform(get("/api/usuarios/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void usuarioEditaOProprioPerfilENormalizaOsLinks() throws Exception {
        Conta aluna = helper.novaConta("Ana", "STUDENT");

        Map<String, Object> dados = new HashMap<>();
        dados.put("name", " Ana Martins ");
        dados.put("bio", "Construo APIs.");
        dados.put("professionalTitle", "Dev em formação");
        dados.put("github", "github.com/ana-martins");
        dados.put("linkedin", "https://www.linkedin.com/in/ana-martins/");

        mvc.perform(put("/api/usuarios/me")
                        .header("Authorization", bearer(aluna.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(dados)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Martins"))
                .andExpect(jsonPath("$.github").value("https://github.com/ana-martins"))
                .andExpect(jsonPath("$.linkedin").value("https://linkedin.com/in/ana-martins"));

        mvc.perform(get("/api/usuarios/" + aluna.id()))
                .andExpect(jsonPath("$.bio").value("Construo APIs."))
                .andExpect(jsonPath("$.professionalTitle").value("Dev em formação"));

        // Campos vazios limpam o perfil
        mvc.perform(put("/api/usuarios/me")
                        .header("Authorization", bearer(aluna.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("name", "Ana", "github", "", "bio", " "))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.github").isEmpty())
                .andExpect(jsonPath("$.bio").isEmpty());

        mvc.perform(put("/api/usuarios/me").contentType(MediaType.APPLICATION_JSON).content(helper.toJson(dados)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void linkQueNaoEDoGitHubOuLinkedInERecusado() throws Exception {
        Conta aluna = helper.novaConta("Ana", "STUDENT");

        for (String ataque : new String[] {"javascript:alert(1)", "https://site-falso.com/github.com/ana"}) {
            mvc.perform(put("/api/usuarios/me")
                            .header("Authorization", bearer(aluna.token()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(helper.toJson(Map.of("name", "Ana", "github", ataque, "linkedin", ataque))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.github").exists())
                    .andExpect(jsonPath("$.linkedin").exists());
        }

        // O cadastro também aceita esses campos, então tem a mesma regra
        mvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of(
                                "name", "Bia",
                                "email", TestHelper.emailUnico("bia"),
                                "password", TestHelper.SENHA,
                                "type", "STUDENT",
                                "github", "javascript:alert(1)"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.github").exists());
    }

    @Test
    void minhasCandidaturasTrazemSoAsDoAlunoComAAvaliacao() throws Exception {
        String mentor = helper.novoUsuarioComToken("Rafael", "MENTOR");
        Conta ana = helper.novaConta("Ana", "STUDENT");
        Conta bia = helper.novaConta("Bia", "STUDENT");

        long desafio = criarDesafio(mentor);
        long daAna = entregar(ana.token(), desafio);
        entregar(bia.token(), desafio);
        avaliar(mentor, daAna, 7);

        mvc.perform(get("/api/candidaturas/minhas").header("Authorization", bearer(ana.token())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(daAna))
                .andExpect(jsonPath("$[0].desafioId").value(desafio))
                .andExpect(jsonPath("$[0].status").value("AVALIADO"))
                .andExpect(jsonPath("$[0].avaliacao.nota").value(7));

        mvc.perform(get("/api/candidaturas/minhas").header("Authorization", bearer(mentor)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/candidaturas/minhas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void envioSegueMesmoComOGitHubForaDoAr() throws Exception {
        // Nos testes o endereço do GitHub aponta para uma porta fechada (ver application.properties de teste)
        String mentor = helper.novoUsuarioComToken("Rafael", "MENTOR");
        String aluna = helper.novoUsuarioComToken("Ana", "STUDENT");

        long id = entregar(aluna, criarDesafio(mentor));

        mvc.perform(get("/api/candidaturas/" + id).header("Authorization", bearer(aluna)))
                .andExpect(jsonPath("$.status").value("ENVIADO"))
                .andExpect(jsonPath("$.linkEnvio").value("https://github.com/ana/api-pedidos"))
                .andExpect(jsonPath("$.repoLinguagem").isEmpty());
    }

    private JsonNode metricas() throws Exception {
        return helper.readJson(mvc.perform(get("/api/metricas"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
    }

    @Test
    void metricasSaoPublicasESomamOQueAconteceu() throws Exception {
        JsonNode antes = metricas();

        String mentor = helper.novoUsuarioComToken("Rafael", "MENTOR");
        Conta ana = helper.novaConta("Ana", "STUDENT");
        avaliar(mentor, entregar(ana.token(), criarDesafio(mentor)), 8);

        JsonNode depois = metricas();

        assertEquals(antes.get("desafios").asLong() + 1, depois.get("desafios").asLong());
        assertEquals(antes.get("inscricoes").asLong() + 1, depois.get("inscricoes").asLong());
        assertEquals(antes.get("enviados").asLong() + 1, depois.get("enviados").asLong());
        assertEquals(antes.get("avaliados").asLong() + 1, depois.get("avaliados").asLong());

        JsonNode meses = depois.get("inscricoesPorMes");
        assertEquals(6, meses.size());
        assertEquals(YearMonth.now().toString(), meses.get(5).get("mes").asText());
        assertEquals(antes.get("inscricoesPorMes").get(5).get("total").asLong() + 1, meses.get(5).get("total").asLong());
    }
}
