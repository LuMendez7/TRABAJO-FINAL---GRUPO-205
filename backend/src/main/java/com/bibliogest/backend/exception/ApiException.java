package com.bibliogest.backend.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String codigo;
    private final String campo;

    private ApiException(HttpStatus status, String codigo, String mensaje, String campo) {
        super(mensaje);
        this.status = status;
        this.codigo = codigo;
        this.campo = campo;
    }

    public static ApiException validacion(String mensaje, String campo) {
        return new ApiException(HttpStatus.BAD_REQUEST, "VALIDACION", mensaje, campo);
    }

    public static ApiException noEncontrado(String mensaje) {
        return new ApiException(HttpStatus.NOT_FOUND, "NO_ENCONTRADO", mensaje, null);
    }

    public static ApiException conflicto(String mensaje, String campo) {
        return new ApiException(HttpStatus.CONFLICT, "CONFLICTO", mensaje, campo);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getCampo() {
        return campo;
    }
}
