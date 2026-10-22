package com.bibliogest.backend.controller;

import com.bibliogest.backend.entity.Usuario;
import com.bibliogest.backend.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    public Usuario buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario crear(@RequestBody Usuario usuario) {
        return usuarioService.crear(usuario);
    }

    @PutMapping("/{id}")
    public Usuario actualizar(
            @PathVariable Long id,
            @RequestBody Usuario usuario) {

        return usuarioService.actualizar(id, usuario);
    }

    @PutMapping("/{id}/desactivar")
    public Usuario desactivar(@PathVariable Long id) {
        return usuarioService.desactivar(id);
    }

    @GetMapping("/activos")
    public List<Usuario> listarActivos() {
        return usuarioService.listarActivos();
    }
}
