package com.bibliogest.backend.security;

import com.bibliogest.backend.entity.Usuario;
import com.bibliogest.backend.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class BibliogestUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public BibliogestUserDetailsService(
            UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        if (email == null || email.isBlank()) {
            throw new UsernameNotFoundException("Usuario no encontrado");
        }
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado"));

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .roles(usuario.getRol())
                .disabled(!Boolean.TRUE.equals(usuario.getActivo()))
                .build();
    }
}
