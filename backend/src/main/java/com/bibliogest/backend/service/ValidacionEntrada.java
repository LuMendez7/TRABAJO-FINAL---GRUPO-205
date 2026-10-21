package com.bibliogest.backend.service;

import com.bibliogest.backend.exception.ApiException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;
import java.util.regex.Pattern;

final class ValidacionEntrada {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private ValidacionEntrada() {
    }

    static String obligatorio(String valor, String campo, int maximo) {
        String normalizado = valor == null ? null : valor.trim();
        if (normalizado == null || normalizado.isEmpty()) {
            throw ApiException.validacion("El campo " + campo + " es obligatorio", campo);
        }
        if (normalizado.length() > maximo) {
            throw ApiException.validacion("El campo " + campo + " excede " + maximo + " caracteres", campo);
        }
        return normalizado;
    }

    static String opcional(String valor, String campo, int maximo) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        String normalizado = valor.trim();
        if (normalizado.length() > maximo) {
            throw ApiException.validacion("El campo " + campo + " excede " + maximo + " caracteres", campo);
        }
        return normalizado;
    }

    static String email(String valor) {
        String normalizado = obligatorio(valor, "email", 150).toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(normalizado).matches()) {
            throw ApiException.validacion("El email es invalido", "email");
        }
        return normalizado;
    }

    static void password(String valor) {
        if (valor == null || valor.codePointCount(0, valor.length()) < 8
                || valor.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw ApiException.validacion("La password debe tener entre 8 caracteres y 72 bytes UTF-8",
                    "password");
        }
    }

    static Long id(Long valor, String campo) {
        if (valor == null || valor <= 0) {
            throw ApiException.validacion("El identificador de " + campo + " debe ser positivo", campo);
        }
        return valor;
    }

    static void anio(Integer valor) {
        int actual = LocalDate.now(ZoneId.of("America/Argentina/Buenos_Aires")).getYear();
        if (valor != null && (valor < 1450 || valor > actual)) {
            throw ApiException.validacion("El anio de publicacion es invalido", "anioPublicacion");
        }
    }
}
