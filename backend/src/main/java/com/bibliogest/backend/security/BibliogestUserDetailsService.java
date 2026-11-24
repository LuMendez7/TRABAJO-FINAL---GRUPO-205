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

        String emailNormalizado =
                email.trim().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository
                .findByEmail(emailNormalizado)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado"));

        boolean cuentaHabilitada =
                Boolean.TRUE.equals(usuario.getActivo()) &&
                "APROBADO".equals(usuario.getEstadoCuenta());

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .roles(usuario.getRol())
                .disabled(!cuentaHabilitada)
                .build();
    }
}