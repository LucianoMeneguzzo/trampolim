package com.trampolim.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trampolim.api.service.NotificacaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static com.trampolim.api.TestHelper.bearer;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Confere que cada etapa do fluxo avisa a pessoa certa. O NotificacaoService aqui é um dublê:
// o teste só verifica quem seria avisado, sem enviar e-mail.
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:notificacoes;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class NotificacoesIntegrationTest {

    private static final String TITULO = "API de pedidos para uma pequena loja";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean NotificacaoService notificacoes;

    @Test
    void cadaEtapaAvisaAPessoaCerta() throws Exception {
        TestHelper helper = new TestHelper(mvc, objectMapper);
        TestHelper.Conta mentor = helper.novaConta("Rafael", "MENTOR");
        TestHelper.Conta aluno = helper.novaConta("Ana", "STUDENT");

        String desafio = mvc.perform(post("/api/desafios")
                        .header("Authorization", bearer(mentor.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("titulo", TITULO, "descricao", "Sair da planilha para uma API.",
                                "tecnologias", "Java, Spring Boot", "nivel", "INICIANTE", "prazo", "2099-12-31",
                                "vagas", 5, "empresa", "Loja da Esquina"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long desafioId = helper.readJson(desafio).get("id").asLong();

        // 1. Inscrição: o mentor é avisado
        String candidatura = mvc.perform(post("/api/candidaturas")
                        .header("Authorization", bearer(aluno.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("desafioId", desafioId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long candidaturaId = helper.readJson(candidatura).get("id").asLong();
        verify(notificacoes).novaInscricao(mentor.email(), "Rafael", "Ana", TITULO);

        // 2. Aprovação: o aluno é avisado
        mvc.perform(patch("/api/candidaturas/" + candidaturaId + "/status")
                        .header("Authorization", bearer(mentor.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("status", "APROVADO"))))
                .andExpect(status().isOk());
        verify(notificacoes).inscricaoDecidida(aluno.email(), "Ana", TITULO, true);

        // 3. Envio do projeto: o mentor é avisado
        mvc.perform(put("/api/candidaturas/" + candidaturaId + "/envio")
                        .header("Authorization", bearer(aluno.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("linkEnvio", "github.com/ana/api-pedidos"))))
                .andExpect(status().isOk());
        verify(notificacoes).projetoEnviado(mentor.email(), "Rafael", "Ana", TITULO, candidaturaId);

        // 4. Avaliação: o aluno é avisado, com a nota
        mvc.perform(post("/api/candidaturas/" + candidaturaId + "/avaliacao")
                        .header("Authorization", bearer(mentor.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("nota", 9, "feedback", "Código claro e bem organizado, parabéns."))))
                .andExpect(status().isCreated());
        verify(notificacoes).entregaAvaliada(aluno.email(), "Ana", TITULO, 9, aluno.id());
    }

    @Test
    void acaoRecusadaNaoAvisaNinguem() throws Exception {
        TestHelper helper = new TestHelper(mvc, objectMapper);
        TestHelper.Conta aluno = helper.novaConta("Bia", "STUDENT");

        mvc.perform(post("/api/candidaturas")
                        .header("Authorization", bearer(aluno.token()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("desafioId", 999999))))
                .andExpect(status().isNotFound());

        verify(notificacoes, never()).novaInscricao(any(), any(), any(), any());
        verify(notificacoes, never()).inscricaoDecidida(any(), any(), any(), anyBoolean());
    }
}
