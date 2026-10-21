package com.bibliogest.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import com.bibliogest.backend.repository.UsuarioRepository;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {
	@Autowired
	private UsuarioRepository usuarios;

	@Test
	void contextLoads() {
		assertEquals(0, usuarios.count());
	}

}
