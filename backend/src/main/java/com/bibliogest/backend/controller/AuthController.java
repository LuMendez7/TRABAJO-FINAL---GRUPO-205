package com.bibliogest.backend.controller;

import com.bibliogest.backend.entity.Usuario;
import com.bibliogest.backend.service.UsuarioService;
import com.bibliogest.backend.security.SesionTokenService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final SesionTokenService tokens;

    public AuthController(
            UsuarioService usuarioService,
            SesionTokenService tokens) {
        this.usuarioService = usuarioService;
        this.tokens = tokens;
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

    @PostMapping("/login")
    public Map<String, String> iniciarSesion(
            Authentication authentication) {

        Map<String, String> usuario =
                usuarioActual(authentication);

        String token = tokens.crear(
                authentication.getName()
        );

        return Map.of(
                "email", usuario.get("email"),
                "rol", usuario.get("rol"),
                "token", token
        );
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cerrarSesion(
            @RequestHeader(
                value = "Authorization",
                required = false
            ) String autorizacion) {

        if (autorizacion != null
                && autorizacion.startsWith("Bearer ")) {
            tokens.invalidar(autorizacion.substring(7));
        }
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