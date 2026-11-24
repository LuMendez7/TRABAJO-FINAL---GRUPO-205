package com.bibliogest.backend.controller;

import com.bibliogest.backend.entity.Usuario;
import com.bibliogest.backend.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

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

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> registrar(
            @RequestBody Usuario datos) {

        Usuario usuario = usuarioService.registrar(datos);

        return Map.of(
                "mensaje", "Solicitud de registro enviada correctamente.",
                "email", usuario.getEmail(),
                "estadoCuenta", usuario.getEstadoCuenta()
        );
    }
}