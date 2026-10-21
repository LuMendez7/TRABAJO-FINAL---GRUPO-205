package com.bibliogest.backend.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, String>> api(ApiException ex) {
        return respuesta(ex.getStatus(), ex.getCodigo(), ex.getMessage(), ex.getCampo());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> estado(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String codigo = switch (status) {
            case BAD_REQUEST -> "VALIDACION";
            case NOT_FOUND -> "NO_ENCONTRADO";
            case CONFLICT, FORBIDDEN -> "CONFLICTO";
            default -> "ERROR_INTERNO";
        };
        return respuesta(status, codigo, ex.getReason() == null ? "Operacion rechazada" : ex.getReason(), null);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentNotValidException.class,
            ConstraintViolationException.class})
    public ResponseEntity<Map<String, String>> entradaInvalida(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException validacion
                && validacion.getBindingResult().getFieldError() != null) {
            var error = validacion.getBindingResult().getFieldError();
            return respuesta(HttpStatus.BAD_REQUEST, "VALIDACION",
                    error.getDefaultMessage() == null ? "Entrada invalida" : error.getDefaultMessage(),
                    error.getField());
        }
        return respuesta(HttpStatus.BAD_REQUEST, "VALIDACION", "Cuerpo JSON o datos invalidos", null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> rutaInvalida(MethodArgumentTypeMismatchException ex) {
        return respuesta(HttpStatus.BAD_REQUEST, "VALIDACION", "Parametro de ruta invalido", ex.getName());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> entidadNoEncontrada(EntityNotFoundException ex) {
        return respuesta(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", "Recurso no encontrado", null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> integridad(DataIntegrityViolationException ex) {
        return respuesta(HttpStatus.CONFLICT, "CONFLICTO", "Conflicto de unicidad o referencia", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> inesperado(Exception ex) {
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Error interno", null);
    }

    private ResponseEntity<Map<String, String>> respuesta(
            HttpStatus status, String codigo, String mensaje, String campo) {
        Map<String, String> cuerpo = new LinkedHashMap<>();
        cuerpo.put("codigo", codigo);
        cuerpo.put("mensaje", mensaje);
        if (campo != null) {
            cuerpo.put("campo", campo);
        }
        return ResponseEntity.status(status).body(cuerpo);
    }
}
