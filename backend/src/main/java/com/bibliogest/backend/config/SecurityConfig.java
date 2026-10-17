
package com.bibliogest.backend.config;

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

                    config.setAllowedOrigins(List.of(
                            "http://localhost:5173",
                            "http://localhost:5174"
                    ));

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
