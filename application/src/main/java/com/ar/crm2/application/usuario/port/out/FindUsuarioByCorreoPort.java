package com.ar.crm2.application.usuario.port.out;

import com.ar.crm2.model.entity.Usuario;

import java.util.Optional;

public interface FindUsuarioByCorreoPort {

    Optional<Usuario> findByCorreo(String correo);
}
