package br.com.ecociente.senha.dataprovaider.mapper;

import br.com.ecociente.senha.core.domain.Usuario;
import br.com.ecociente.senha.dataprovaider.entity.UsuarioEntity;

import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public Usuario toDomain(UsuarioEntity entity) {
        return Usuario.builder()
                .id(entity.getIdUsuario())
                .email(entity.getEmailUsuario())
                .senha(entity.getSenhaHash())
                .build();
    }
}