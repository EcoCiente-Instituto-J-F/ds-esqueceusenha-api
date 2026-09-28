package br.com.ecociente.senha.core.gateway;

import br.com.ecociente.senha.core.domain.Usuario;

import java.util.Optional;

public interface UsuarioGateway {

    Optional<Usuario> buscarPorEmail(String email);

    Optional<Usuario> buscarPorId(Integer id);

    void atualizarSenha(Integer usuarioId, String senhaHash);
}