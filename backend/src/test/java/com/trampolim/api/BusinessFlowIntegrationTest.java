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

import static com.trampolim.api.TestHelper.bearer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Fluxo da área /business: mentor publica desafio (post-challenge) e avalia a entrega (assessment/:id)
@SpringBootTest
@AutoConfigureMockMvc
class BusinessFlowIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    TestHelper helper;

    @BeforeEach
    void setUp() {
        helper = new TestHelper(mvc, objectMapper);
    }

    private Map<String, Object> desafioValido() {
        return Map.of(
                "titulo", "API de pedidos para uma pequena loja",
                "descricao", "A loja precisa sair da planilha e registrar clientes, produtos e pedidos.",
                "nivel", "intermediario",
                "tecnologias", "Java, Spring Boot",
                "empresa", "Loja Horizonte",
                "prazo", "2030-12-31",
                "vagas", 20);
    }

    private long criarDesafio(String tokenMentor) throws Exception {
        String resposta = mvc.perform(post("/api/desafios")
                        .header("Authorization", bearer(tokenMentor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(desafioValido())))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return helper.readJson(resposta).get("id").asLong();
    }

    private long inscrever(String tokenAluno, long desafioId) throws Exception {
        String resposta = mvc.perform(post("/api/candidaturas")
                        .header("Authorization", bearer(tokenAluno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("desafioId", desafioId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return helper.readJson(resposta).get("id").asLong();
    }

    private Map<String, Object> avaliacao(int nota) {
        return Map.of("nota", nota, "feedback", "A estrutura da API está clara e as respostas de erro estão bem pensadas.");
    }

    @Test
    void mentorPublicaDesafioComTodosOsCampos() throws Exception {
        String mentor = helper.novoUsuarioComToken("Rafael", "MENTOR");

        mvc.perform(post("/api/desafios")
                        .header("Authorization", bearer(mentor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(desafioValido())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titulo").value("API de pedidos para uma pequena loja"))
                .andExpect(jsonPath("$.nivel").value("INTERMEDIARIO"))
                .andExpect(jsonPath("$.tecnologias").value("Java, Spring Boot"))
                .andExpect(jsonPath("$.empresa").value("Loja Horizonte"))
                .andExpect(jsonPath("$.prazo").value("2030-12-31"))
                .andExpect(jsonPath("$.vagas").value(20))
                .andExpect(jsonPath("$.nomeCriador").value("Rafael"));
    }

    @Test
    void lerDesafiosExigeLogin() throws Exception {
        String mentor = helper.novoUsuarioComToken("Marina", "MENTOR");
        long id = criarDesafio(mentor);

        mvc.perform(get("/api/desafios"))
                .andExpect(status().isUnauthorized());

        mvc.perform(get("/api/desafios/" + id).header("Authorization", bearer(mentor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nomeCriador").value("Marina"));
        mvc.perform(get("/api/desafios").header("Authorization", bearer(mentor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeCriador").exists());
        mvc.perform(get("/api/desafios/999999").header("Authorization", bearer(mentor)))
                .andExpect(status().isNotFound());
    }

    @Test
    void alunoNaoPodePublicarDesafio() throws Exception {
        String aluno = helper.novoUsuarioComToken("Ana", "STUDENT");

        mvc.perform(post("/api/desafios")
                        .header("Authorization", bearer(aluno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(desafioValido())))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/desafios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(desafioValido())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void desafioComCamposInvalidosRetorna400() throws Exception {
        String mentor = helper.novoUsuarioComToken("Caio", "MENTOR");

        mvc.perform(post("/api/desafios")
                        .header("Authorization", bearer(mentor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("titulo", "X", "descricao", "Y", "nivel", "EXPERT"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nivel").exists())
                .andExpect(jsonPath("$.tecnologias").exists())
                .andExpect(jsonPath("$.prazo").exists())
                .andExpect(jsonPath("$.vagas").exists())
                .andExpect(jsonPath("$.empresa").exists());
    }

    @Test
    void fluxoCompletoDeAvaliacao() throws Exception {
        String mentor = helper.novoUsuarioComToken("Rafael", "MENTOR");
        String outroMentor = helper.novoUsuarioComToken("Outro", "MENTOR");
        String aluno = helper.novoUsuarioComToken("Ana", "STUDENT");

        long desafioId = criarDesafio(mentor);
        long candidaturaId = inscrever(aluno, desafioId);

        // Link que não é de repositório do GitHub
        mvc.perform(put("/api/candidaturas/" + candidaturaId + "/envio")
                        .header("Authorization", bearer(aluno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("linkEnvio", "https://exemplo.com/projeto"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.linkEnvio").exists());

        // Aluno envia o repositório
        mvc.perform(put("/api/candidaturas/" + candidaturaId + "/envio")
                        .header("Authorization", bearer(aluno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("linkEnvio", "github.com/ana/api-pedidos"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENVIADO"))
                .andExpect(jsonPath("$.linkEnvio").value("https://github.com/ana/api-pedidos"));

        // Outro mentor não vê a inscrição, não lista as inscrições do desafio e não avalia
        mvc.perform(get("/api/candidaturas/" + candidaturaId).header("Authorization", bearer(outroMentor)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/candidaturas/desafio/" + desafioId).header("Authorization", bearer(outroMentor)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/candidaturas/" + candidaturaId + "/avaliacao")
                        .header("Authorization", bearer(outroMentor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(avaliacao(8))))
                .andExpect(status().isForbidden());

        // Mentor dono abre a tela de avaliação
        mvc.perform(get("/api/candidaturas/" + candidaturaId).header("Authorization", bearer(mentor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCandidato").value("Ana"))
                .andExpect(jsonPath("$.tituloDesafio").value("API de pedidos para uma pequena loja"))
                .andExpect(jsonPath("$.linkEnvio").value("https://github.com/ana/api-pedidos"))
                .andExpect(jsonPath("$.avaliacao").isEmpty());

        // Nota fora da faixa
        mvc.perform(post("/api/candidaturas/" + candidaturaId + "/avaliacao")
                        .header("Authorization", bearer(mentor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(avaliacao(11))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.nota").exists());

        // Aluno não pode se avaliar
        mvc.perform(post("/api/candidaturas/" + candidaturaId + "/avaliacao")
                        .header("Authorization", bearer(aluno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(avaliacao(10))))
                .andExpect(status().isForbidden());

        // Avaliação válida
        mvc.perform(post("/api/candidaturas/" + candidaturaId + "/avaliacao")
                        .header("Authorization", bearer(mentor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(avaliacao(8))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nota").value(8))
                .andExpect(jsonPath("$.nomeMentor").value("Rafael"))
                .andExpect(jsonPath("$.nomeCandidato").value("Ana"));

        // Segunda avaliação da mesma entrega
        mvc.perform(post("/api/candidaturas/" + candidaturaId + "/avaliacao")
                        .header("Authorization", bearer(mentor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(avaliacao(9))))
                .andExpect(status().isConflict());

        // O aluno vê a própria entrega já avaliada
        mvc.perform(get("/api/candidaturas/" + candidaturaId).header("Authorization", bearer(aluno)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVALIADO"))
                .andExpect(jsonPath("$.avaliacao.nota").value(8));

        // Depois de avaliada, não aceita novo envio
        mvc.perform(put("/api/candidaturas/" + candidaturaId + "/envio")
                        .header("Authorization", bearer(aluno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("linkEnvio", "github.com/ana/outro-projeto"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void naoAvaliaInscricaoSemProjetoEnviado() throws Exception {
        String mentor = helper.novoUsuarioComToken("Rafael", "MENTOR");
        String aluno = helper.novoUsuarioComToken("Ana", "STUDENT");
        long candidaturaId = inscrever(aluno, criarDesafio(mentor));

        mvc.perform(post("/api/candidaturas/" + candidaturaId + "/avaliacao")
                        .header("Authorization", bearer(mentor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(avaliacao(7))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Esta inscrição ainda não tem projeto enviado."));
    }

    @Test
    void inscricaoRepetidaRetorna409() throws Exception {
        String mentor = helper.novoUsuarioComToken("Rafael", "MENTOR");
        String aluno = helper.novoUsuarioComToken("Ana", "STUDENT");
        long desafioId = criarDesafio(mentor);
        inscrever(aluno, desafioId);

        mvc.perform(post("/api/candidaturas")
                        .header("Authorization", bearer(aluno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("desafioId", desafioId))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Você já se candidatou para este desafio."));
    }

    @Test
    void candidaturaInexistenteRetorna404() throws Exception {
        String mentor = helper.novoUsuarioComToken("Rafael", "MENTOR");

        mvc.perform(get("/api/candidaturas/999999").header("Authorization", bearer(mentor)))
                .andExpect(status().isNotFound());
    }
}
