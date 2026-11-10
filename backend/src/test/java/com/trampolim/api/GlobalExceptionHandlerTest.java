package com.trampolim.api;

import com.trampolim.api.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void erroInesperadoNaoExpoeOTextoInterno() {
        ResponseEntity<Map<String, String>> resposta =
                handler.handleGenericException(new IllegalStateException("select senha from usuarios"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resposta.getStatusCode());
        assertFalse(resposta.getBody().get("erro").contains("usuarios"));
    }

    @Test
    void violacaoDoBancoViraConflitoSemExporSql() {
        ResponseEntity<Map<String, String>> resposta = handler.handleDataIntegrity(
                new DataIntegrityViolationException("erro", new SQLException("duplicate key usuarios_email_key")));

        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
        assertFalse(resposta.getBody().get("erro").contains("usuarios_email_key"));
    }
}
