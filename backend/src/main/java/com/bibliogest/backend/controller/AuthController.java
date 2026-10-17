
package com.bibliogest.backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public Map<String, String> usuarioActual(
            Authentication authentication) {

        String rol = authentication.getAuthorities()
                .stream()
                .map(autoridad -> autoridad.getAuthority())
                .filter(autoridad -> autoridad.startsWith("ROLE_"))
                .map(autoridad -> autoridad.substring(5))
                .findFirst()
                .orElse("SIN_ROL");

        return Map.of(
                "email", authentication.getName(),
                "rol", rol
        );
    }
}
