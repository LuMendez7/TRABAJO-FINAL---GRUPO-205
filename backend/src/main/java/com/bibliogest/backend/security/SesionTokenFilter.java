package com.bibliogest.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class SesionTokenFilter extends OncePerRequestFilter {

    private final SesionTokenService tokens;
    private final BibliogestUserDetailsService usuarios;

    public SesionTokenFilter(
            SesionTokenService tokens,
            BibliogestUserDetailsService usuarios) {
        this.tokens = tokens;
        this.usuarios = usuarios;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);
            String email = tokens.validar(token);

            if (email == null) {
                response.sendError(401);
                return;
            }

            try {
                UserDetails usuario =
                        usuarios.loadUserByUsername(email);

                if (!usuario.isEnabled()
                        || !usuario.isAccountNonLocked()
                        || !usuario.isAccountNonExpired()
                        || !usuario.isCredentialsNonExpired()) {

                    tokens.invalidar(token);
                    response.sendError(401);
                    return;
                }

                var autenticacion =
                        new UsernamePasswordAuthenticationToken(
                                usuario.getUsername(),
                                null,
                                usuario.getAuthorities()
                        );

                var contexto =
                        SecurityContextHolder.createEmptyContext();

                contexto.setAuthentication(autenticacion);
                SecurityContextHolder.setContext(contexto);

            } catch (UsernameNotFoundException ex) {
                tokens.invalidar(token);
                response.sendError(401);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}