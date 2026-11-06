package com.trampolim.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.trampolim.api.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Confirma na API do GitHub que o repositório enviado pelo aluno existe e é público,
// e traz a linguagem principal e a data do último commit.
@Service
public class GitHubService {

    private static final Logger log = LoggerFactory.getLogger(GitHubService.class);
    private static final Pattern DONO_E_REPOSITORIO = Pattern.compile("github\\.com/([\\w.-]+)/([\\w.-]+?)(?:\\.git)?/?$");

    public record Repositorio(String linguagem, LocalDateTime ultimoCommit) {
        static final Repositorio SEM_DADOS = new Repositorio(null, null);
    }

    private final RestClient client;

    public GitHubService(@Value("${app.github.api-url:https://api.github.com}") String apiUrl,
                         @Value("${app.github.token:}") String token) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(3000);
        fabrica.setReadTimeout(5000);

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(apiUrl)
                .requestFactory(fabrica)
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("User-Agent", "trampolim-api");
        // Sem token o GitHub limita a 60 consultas por hora por IP
        if (!token.isBlank()) {
            builder.defaultHeader("Authorization", "Bearer " + token);
        }
        this.client = builder.build();
    }

    // Repositório inexistente ou privado -> 400, a pessoa precisa corrigir o link.
    // GitHub fora do ar ou limite de consultas atingido -> aceita o envio sem os dados:
    // a instabilidade de um serviço externo não pode impedir o aluno de entregar.
    public Repositorio consultarRepositorio(String link) {
        Matcher partes = DONO_E_REPOSITORIO.matcher(link);
        if (!partes.find()) {
            throw ApiException.badRequest("Use o link de um repositório do GitHub, ex: github.com/usuario/projeto");
        }

        try {
            JsonNode resposta = client.get()
                    .uri("/repos/{dono}/{repositorio}", partes.group(1), partes.group(2))
                    .retrieve()
                    .body(JsonNode.class);

            return new Repositorio(texto(resposta, "language"), data(texto(resposta, "pushed_at")));
        } catch (HttpClientErrorException.NotFound e) {
            throw ApiException.badRequest("Não encontramos esse repositório no GitHub. Confira o link e se ele é público.");
        } catch (RestClientException e) {
            log.warn("Não foi possível consultar o GitHub: {}", e.getMessage());
            return Repositorio.SEM_DADOS;
        }
    }

    private static String texto(JsonNode json, String campo) {
        return json != null && json.hasNonNull(campo) ? json.get(campo).asText() : null;
    }

    private static LocalDateTime data(String texto) {
        if (texto == null) return null;
        try {
            return OffsetDateTime.parse(texto).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
