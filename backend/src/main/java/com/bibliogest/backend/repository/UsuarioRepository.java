package com.bibliogest.backend.repository;

import com.bibliogest.backend.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    List<Usuario> findByActivoTrue();
}