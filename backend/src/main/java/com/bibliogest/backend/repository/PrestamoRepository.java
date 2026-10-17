
package com.bibliogest.backend.repository;

import com.bibliogest.backend.entity.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    List<Prestamo> findByUsuario_Email(String email);

}
