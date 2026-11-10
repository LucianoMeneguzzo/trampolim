package com.trampolim.api;

import com.sun.net.httpserver.HttpServer;
import com.trampolim.api.exception.ApiException;
import com.trampolim.api.service.GitHubService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Usa um servidor local no lugar do GitHub: o teste não depende de internet nem do limite de consultas
class GitHubServiceTest {

    private HttpServer servidor;
    private String caminhoPedido;
    private String autorizacaoRecebida;
    private int statusDaResposta = 200;
    private String corpoDaResposta = "{}";

    @BeforeEach
    void subirServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/", troca -> {
            caminhoPedido = troca.getRequestURI().getPath();
            autorizacaoRecebida = troca.getRequestHeaders().getFirst("Authorization");
            troca.getResponseHeaders().set("Content-Type", "application/json");
            byte[] corpo = corpoDaResposta.getBytes(StandardCharsets.UTF_8);
            troca.sendResponseHeaders(statusDaResposta, corpo.length);
            troca.getResponseBody().write(corpo);
            troca.close();
        });
        servidor.start();
    }

    @AfterEach
    void pararServidor() {
        servidor.stop(0);
    }

    private GitHubService servico(String token) {
        return new GitHubService("http://localhost:" + servidor.getAddress().getPort(), token);
    }

    @Test
    void trazLinguagemEUltimoCommitDoRepositorio() {
        corpoDaResposta = "{\"language\":\"Java\",\"pushed_at\":\"2026-10-20T15:30:00Z\",\"private\":false}";

        GitHubService.Repositorio repo = servico("").consultarRepositorio("https://github.com/ana/api-pedidos");

        assertEquals("/repos/ana/api-pedidos", caminhoPedido);
        assertNull(autorizacaoRecebida);
        assertEquals("Java", repo.linguagem());
        assertEquals(OffsetDateTime.parse("2026-10-20T15:30:00Z").atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(), repo.ultimoCommit());
    }

    @Test
    void aceitaLinkTerminadoEmGitEEnviaOToken() {
        corpoDaResposta = "{\"language\":null,\"pushed_at\":null}";

        GitHubService.Repositorio repo = servico("meu-token").consultarRepositorio("https://github.com/ana/projeto.git");

        assertEquals("/repos/ana/projeto", caminhoPedido);
        assertEquals("Bearer meu-token", autorizacaoRecebida);
        assertNull(repo.linguagem());
        assertNull(repo.ultimoCommit());
    }

    @Test
    void repositorioInexistenteOuPrivadoRetorna400() {
        statusDaResposta = 404;

        ApiException erro = assertThrows(ApiException.class,
                () -> servico("").consultarRepositorio("https://github.com/ana/nao-existe"));

        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatus());
    }

    @Test
    void limiteDeConsultasNaoImpedeOEnvio() {
        statusDaResposta = 403;

        GitHubService.Repositorio repo = servico("").consultarRepositorio("https://github.com/ana/projeto");

        assertNull(repo.linguagem());
        assertNull(repo.ultimoCommit());
    }

    @Test
    void githubForaDoArNaoImpedeOEnvio() {
        statusDaResposta = 503;

        GitHubService.Repositorio repo = servico("").consultarRepositorio("https://github.com/ana/projeto");

        assertNull(repo.linguagem());
    }

    @Test
    void dataEmFormatoInesperadoEIgnorada() {
        corpoDaResposta = "{\"language\":\"Go\",\"pushed_at\":\"ontem\"}";

        GitHubService.Repositorio repo = servico("").consultarRepositorio("https://github.com/ana/projeto");

        assertEquals("Go", repo.linguagem());
        assertNull(repo.ultimoCommit());
    }

    @Test
    void linkQueNaoEDoGitHubRetorna400SemChamarNada() {
        ApiException erro = assertThrows(ApiException.class,
                () -> servico("").consultarRepositorio("https://exemplo.com/ana/projeto"));

        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatus());
        assertNull(caminhoPedido);
    }
}
