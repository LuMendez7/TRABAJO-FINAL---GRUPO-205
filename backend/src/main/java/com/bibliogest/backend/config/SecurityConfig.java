
package com.bibliogest.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${BIBLIOGEST_CORS_ALLOWED_ORIGINS:http://localhost:5173,http://localhost:5174}")
            String configuredOrigins)
            throws Exception {

        List<String> allowedOrigins = Arrays.stream(configuredOrigins.split(",", -1))
                .map(String::trim)
                .toList();
        if (allowedOrigins.stream().anyMatch(origin -> origin.isEmpty() || origin.contains("*"))) {
            throw new IllegalArgumentException("La configuracion de origenes CORS es invalida");
        }

        http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();

                    config.setAllowedOrigins(allowedOrigins);

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
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/libros/activos",
                                "/api/ejemplares/activos",
                                "/api/categorias/**",
                                "/api/autores/**"
                        ).permitAll()
                        .requestMatchers("/api/auth/me").authenticated()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/prestamos/mis-prestamos"
                        ).authenticated()

                        .requestMatchers("/api/**").hasRole("BIBLIOTECARIO")
                        .anyRequest().denyAll()
                )
                .httpBasic(basic -> {});

        return http.build();
    }
}
