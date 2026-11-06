package com.trampolim.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Trata erros de validação dos DTOs (ex: @NotBlank, @NotNull)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // Erros de regra de negócio com status próprio (404, 403, 409...)
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> handleApiException(ApiException ex) {
        return erro(ex.getStatus(), ex.getMessage());
    }

    // JSON malformado ou valor inválido em enum (ex: "type": "Aluno" em vez de "STUDENT")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return erro(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido. Confira o formato dos campos enviados.");
    }

    // Parâmetro de URL com tipo errado (ex: /api/desafios/abc)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return erro(HttpStatus.BAD_REQUEST, "Parâmetro inválido: " + ex.getName());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NoResourceFoundException ex) {
        return erro(HttpStatus.NOT_FOUND, "Rota não encontrada.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return erro(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP não suportado nesta rota.");
    }

    // Trata nossas exceções de regra de negócio (RuntimeException)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        return erro(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Trata qualquer outro erro inesperado (fallback)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception ex) {
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro interno no servidor. Tente novamente mais tarde.");
    }

    private ResponseEntity<Map<String, String>> erro(HttpStatus status, String mensagem) {
        Map<String, String> error = new HashMap<>();
        error.put("erro", mensagem);
        return ResponseEntity.status(status).body(error);
    }
}
