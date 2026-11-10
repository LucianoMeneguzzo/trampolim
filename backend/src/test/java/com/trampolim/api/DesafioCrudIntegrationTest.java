package com.trampolim.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trampolim.api.TestHelper.Conta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static com.trampolim.api.TestHelper.bearer;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// CRUD de desafios: editar, fechar, apagar, "meus desafios" e as regras que barram novas inscrições
@SpringBootTest
@AutoConfigureMockMvc
class DesafioCrudIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper objectMapper;
    TestHelper helper;

    @BeforeEach
    void setUp() {
        helper = new TestHelper(mvc, objectMapper);
    }

    private Map<String, Object> desafio(String titulo) {
        Map<String, Object> dados = new HashMap<>();
        dados.put("titulo", titulo);
        dados.put("descricao", "Uma loja precisa sair da planilha.");
        dados.put("nivel", "INICIANTE");
        dados.put("tecnologias", "Java");
        dados.put("empresa", "Loja Horizonte");
        dados.put("prazo", "2030-12-31");
        dados.put("vagas", 5);
        return dados;
    }

    private long criar(String tokenMentor, Map<String, Object> dados) throws Exception {
        String resposta = mvc.perform(post("/api/desafios")
                        .header("Authorization", bearer(tokenMentor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(dados)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return helper.readJson(resposta).get("id").asLong();
    }

    private org.springframework.test.web.servlet.ResultActions inscrever(String tokenAluno, long desafioId) throws Exception {
        return mvc.perform(post("/api/candidaturas")
                .header("Authorization", bearer(tokenAluno))
                .contentType(MediaType.APPLICATION_JSON)
                .content(helper.toJson(Map.of("desafioId", desafioId))));
    }

    private void alterarStatus(String token, long id, String status) throws Exception {
        mvc.perform(patch("/api/desafios/" + id + "/status")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("status", status))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(status));
    }

    @Test
    void soODonoEditaODesafio() throws Exception {
        String dono = helper.novoUsuarioComToken("Dono", "MENTOR");
        String outro = helper.novoUsuarioComToken("Outro", "MENTOR");
        String aluno = helper.novoUsuarioComToken("Ana", "STUDENT");
        long id = criar(dono, desafio("Título antigo"));

        String corpo = helper.toJson(desafio("Título novo"));

        mvc.perform(put("/api/desafios/" + id).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnauthorized());
        mvc.perform(put("/api/desafios/" + id).header("Authorization", bearer(aluno)).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/desafios/" + id).header("Authorization", bearer(outro)).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isForbidden());

        mvc.perform(put("/api/desafios/" + id).header("Authorization", bearer(dono)).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Título novo"))
                .andExpect(jsonPath("$.nomeCriador").value("Dono"));

        mvc.perform(get("/api/desafios/" + id))
                .andExpect(jsonPath("$.titulo").value("Título novo"));
    }

    @Test
    void desafioFechadoNaoRecebeInscricaoEPodeSerReaberto() throws Exception {
        String mentor = helper.novoUsuarioComToken("Dono", "MENTOR");
        String aluno = helper.novoUsuarioComToken("Ana", "STUDENT");
        long id = criar(mentor, desafio("Fechar e reabrir"));

        alterarStatus(mentor, id, "FECHADO");

        inscrever(aluno, id)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("As inscrições deste desafio estão fechadas."));

        alterarStatus(mentor, id, "ABERTO");

        inscrever(aluno, id).andExpect(status().isCreated());
    }

    @Test
    void statusInvalidoRetorna400EOutroMentorNaoFecha() throws Exception {
        String mentor = helper.novoUsuarioComToken("Dono", "MENTOR");
        String outro = helper.novoUsuarioComToken("Outro", "MENTOR");
        long id = criar(mentor, desafio("Status"));

        mvc.perform(patch("/api/desafios/" + id + "/status")
                        .header("Authorization", bearer(mentor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("status", "BANANA"))))
                .andExpect(status().isBadRequest());

        mvc.perform(patch("/api/desafios/" + id + "/status")
                        .header("Authorization", bearer(outro))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("status", "FECHADO"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void apagaSoDesafioSemInscricoes() throws Exception {
        String mentor = helper.novoUsuarioComToken("Dono", "MENTOR");
        String outro = helper.novoUsuarioComToken("Outro", "MENTOR");
        String aluno = helper.novoUsuarioComToken("Ana", "STUDENT");

        long vazio = criar(mentor, desafio("Sem ninguém"));
        mvc.perform(delete("/api/desafios/" + vazio).header("Authorization", bearer(outro)))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/desafios/" + vazio).header("Authorization", bearer(mentor)))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/desafios/" + vazio))
                .andExpect(status().isNotFound());

        long comInscricao = criar(mentor, desafio("Com inscrição"));
        inscrever(aluno, comInscricao).andExpect(status().isCreated());
        mvc.perform(delete("/api/desafios/" + comInscricao).header("Authorization", bearer(mentor)))
                .andExpect(status().isConflict());
    }

    @Test
    void meusDesafiosTrazContagemSoDoMentorLogado() throws Exception {
        String mentor = helper.novoUsuarioComToken("Dono", "MENTOR");
        String outro = helper.novoUsuarioComToken("Outro", "MENTOR");
        String aluno = helper.novoUsuarioComToken("Ana", "STUDENT");

        long id = criar(mentor, desafio("Do dono"));
        criar(outro, desafio("Do outro"));

        String resposta = inscrever(aluno, id).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long candidatura = helper.readJson(resposta).get("id").asLong();

        mvc.perform(put("/api/candidaturas/" + candidatura + "/envio")
                        .header("Authorization", bearer(aluno))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(helper.toJson(Map.of("linkEnvio", "https://github.com/ana/projeto"))))
                .andExpect(status().isOk());

        mvc.perform(get("/api/desafios/meus").header("Authorization", bearer(mentor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Do dono"))
                .andExpect(jsonPath("$[0].inscritos").value(1))
                .andExpect(jsonPath("$[0].entregas").value(1));

        mvc.perform(get("/api/desafios/" + id))
                .andExpect(jsonPath("$.inscritos").value(1));

        mvc.perform(get("/api/desafios/meus").header("Authorization", bearer(aluno)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/desafios/meus"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void naoInscreveSemVagaOuComPrazoVencido() throws Exception {
        String mentor = helper.novoUsuarioComToken("Dono", "MENTOR");
        String ana = helper.novoUsuarioComToken("Ana", "STUDENT");
        String bia = helper.novoUsuarioComToken("Bia", "STUDENT");

        Map<String, Object> umaVaga = desafio("Uma vaga");
        umaVaga.put("vagas", 1);
        long comUmaVaga = criar(mentor, umaVaga);
        inscrever(ana, comUmaVaga).andExpect(status().isCreated());
        inscrever(bia, comUmaVaga)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("As vagas deste desafio acabaram."));

        Map<String, Object> vencido = desafio("Prazo vencido");
        vencido.put("prazo", LocalDate.now().minusDays(1).toString());
        inscrever(ana, criar(mentor, vencido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O prazo deste desafio já terminou."));
    }

    @Test
    void candidaturasDoDesafioTrazemQuemSeInscreveu() throws Exception {
        String mentor = helper.novoUsuarioComToken("Dono", "MENTOR");
        Conta aluna = helper.novaConta("Ana", "STUDENT");
        long id = criar(mentor, desafio("Lista de inscritos"));
        inscrever(aluna.token(), id).andExpect(status().isCreated());

        mvc.perform(get("/api/candidaturas/desafio/" + id).header("Authorization", bearer(mentor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeCandidato").value("Ana"))
                .andExpect(jsonPath("$[0].candidatoId").value(aluna.id()))
                .andExpect(jsonPath("$[*].desafioId", hasItem((int) id)));
    }
}
