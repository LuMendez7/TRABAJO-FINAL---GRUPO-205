package com.bibliogest.backend.security;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SesionTokenService {

    private static final Duration DURACION = Duration.ofHours(8);

    private final SecureRandom random = new SecureRandom();

    private final ConcurrentHashMap<String, SesionToken> sesiones =
            new ConcurrentHashMap<>();

    private record SesionToken(String email, Instant vencimiento) {}

    public String crear(String email) {
        Instant ahora = Instant.now();

        sesiones.entrySet().removeIf(
                entrada -> !entrada.getValue().vencimiento().isAfter(ahora)
        );

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        sesiones.put(token, new SesionToken(
                email, ahora.plus(DURACION)
        ));

        return token;
    }

    public String validar(String token) {
        SesionToken sesion = sesiones.get(token);

        if (sesion == null) {
            return null;
        }

        if (!sesion.vencimiento().isAfter(Instant.now())) {
            sesiones.remove(token, sesion);
            return null;
        }

        return sesion.email();
    }

    public void invalidar(String token) {
        sesiones.remove(token);
    }
}