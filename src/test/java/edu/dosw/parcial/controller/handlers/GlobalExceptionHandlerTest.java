package edu.dosw.parcial.controller.handlers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleIllegalArgument_returns400WithMessage() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleIllegalArgument(new IllegalArgumentException("Email inválido"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", 400);
        assertThat(response.getBody()).containsEntry("message", "Email inválido");
        assertThat(response.getBody()).containsKey("timestamp");
        assertThat(response.getBody()).containsEntry("error", "Bad Request");
    }

    @Test
    void handleIllegalState_returns409WithMessage() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleIllegalState(new IllegalStateException("Pedido activo existente"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("status", 409);
        assertThat(response.getBody()).containsEntry("message", "Pedido activo existente");
        assertThat(response.getBody()).containsEntry("error", "Conflict");
    }

    @Test
    void handleGeneric_returns500WithGenericMessage() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleGeneric(new RuntimeException("NullPointerException inesperada"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("status", 500);
        assertThat(response.getBody()).containsEntry("message", "Error interno del servidor");
    }

    @Test
    void handleValidation_returns400WithFieldErrors() {
        FieldError fieldError = new FieldError("registerRequest", "email", "Formato de email inválido");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "Error de validación");
        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) response.getBody().get("errors");
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0)).contains("email").contains("Formato de email inválido");
    }

    @Test
    void handleValidation_multipleErrors_returnsAllErrors() {
        FieldError e1 = new FieldError("req", "email", "Email obligatorio");
        FieldError e2 = new FieldError("req", "password", "Contraseña obligatoria");
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(e1, e2));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) response.getBody().get("errors");
        assertThat(errors).hasSize(2);
    }
}
