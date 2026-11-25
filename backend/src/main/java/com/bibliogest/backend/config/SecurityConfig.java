package com.bibliogest.backend.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();

                    List<String> origenes = new java.util.ArrayList<>(List.of(
                            "http://localhost:5173",
                            "http://localhost:5174"
                    ));

                    String frontendUrl = System.getenv("FRONTEND_URL");

                    if (frontendUrl != null && !frontendUrl.isBlank()) {
                        origenes.add(frontendUrl.trim().replaceAll("/+$", ""));
                    }

                    config.setAllowedOrigins(origenes);

                    config.setAllowedMethods(List.of(
                            "GET", "POST", "PUT", "DELETE", "OPTIONS"
                    ));

                    config.setAllowedHeaders(List.of(
                            "Authorization", "Content-Type"
                    ));

                    return config;
                }))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/fichas/**"
                        ).permitAll()

                        // Permitir el procesamiento interno de errores.
                        .dispatcherTypeMatchers(
                                DispatcherType.ERROR
                        ).permitAll()

                        .requestMatchers(
                                HttpMethod.OPTIONS, "/**"
                        ).permitAll()

                        // Catalogo publico.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/libros/activos",
                                "/api/ejemplares/activos",
                                "/api/categorias/**",
                                "/api/autores/**"
                        ).permitAll()

                        // Registro publico de lectores.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/registro"
                        ).permitAll()

                        // Usuario autenticado.
                        .requestMatchers(
                                "/api/auth/me"
                        ).authenticated()

                        // Consulta de prestamos propios.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/prestamos/mis-prestamos"
                        ).authenticated()


                        // Reservas bibliograficas.
                        .requestMatchers(
                            HttpMethod.GET,
                            "/api/reservas/mis-reservas"
                        ).hasRole("LECTOR")

                        .requestMatchers(
                            HttpMethod.POST,
                            "/api/reservas/solicitar"
                        ).hasRole("LECTOR")

                        .requestMatchers(
                            HttpMethod.PUT,
                            "/api/reservas/*/cancelar"
                        ).hasAnyRole("LECTOR", "BIBLIOTECARIO")

                        // Notificaciones personales.
                        .requestMatchers(
                            HttpMethod.GET,
                            "/api/notificaciones",
                            "/api/notificaciones/no-leidas"
                        ).hasAnyRole("LECTOR", "BIBLIOTECARIO")

                        .requestMatchers(
                            HttpMethod.PUT,
                            "/api/notificaciones/*/leer",
                            "/api/notificaciones/leer-todas"
                        ).hasAnyRole("LECTOR", "BIBLIOTECARIO")

                        // Operaciones administrativas.
                        .requestMatchers(
                                "/api/**"
                        ).hasRole("BIBLIOTECARIO")

                        .anyRequest().denyAll()
                )
                .httpBasic(basic -> {});

        return http.build();
    }
}