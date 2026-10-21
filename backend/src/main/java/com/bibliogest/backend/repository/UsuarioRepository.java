
package com.bibliogest.backend.repository;

import com.bibliogest.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    List<Usuario> findByActivoTrue();

    Optional<Usuario> findByEmail(String email);

    boolean existsByRol(String rol);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdUsuarioNot(String email, Long idUsuario);

    Optional<Usuario> findByEmailIgnoreCase(String email);
}
