package com.bibliogest.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:bibliogest_f08_default;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "BIBLIOGEST_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:5174"
})
@ActiveProfiles("test")
class CorsDefaultF08Tests {

    @Autowired private WebApplicationContext contexto;
    @Autowired private FilterChainProxy filtrosSeguridad;

    private MockMvc mvc;

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.webAppContextSetup(contexto).addFilters(filtrosSeguridad).build();
    }

    @Test
    void configuracionLocalSoloPermiteOrigenesPrevistos() throws Exception {
        for (String origin : new String[] {"http://localhost:5173", "http://localhost:5174"}) {
            mvc.perform(options("/api/libros")
                            .header("Origin", origin)
                            .header("Access-Control-Request-Method", "GET"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", origin));
        }
        mvc.perform(options("/api/libros")
                        .header("Origin", "https://desconocido.ejemplo.test")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}
