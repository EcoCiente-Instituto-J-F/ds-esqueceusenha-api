package br.com.ecociente.senha.dataprovaider.gateway;

import java.util.Optional;

import org.springframework.stereotype.Component;

import br.com.ecociente.senha.core.domain.Usuario;
import br.com.ecociente.senha.core.exception.RecursoNaoEncontradoException;
import br.com.ecociente.senha.core.gateway.UsuarioGateway;
import br.com.ecociente.senha.dataprovaider.entity.UsuarioEntity;
import br.com.ecociente.senha.dataprovaider.mapper.UsuarioMapper;
import br.com.ecociente.senha.dataprovaider.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UsuarioGatewayImpl implements UsuarioGateway {

   
    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    @Override
public Optional<Usuario> buscarPorEmail(String email) {
    return usuarioRepository.findByEmailUsuario(email)
            .map(usuarioMapper::toDomain);
}

    @Override
    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id)
                .map(usuarioMapper::toDomain);
    }

    @Override
    public void atualizarSenha(Integer usuarioId, String senhaHash) {
        UsuarioEntity entity = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException("Usuário não encontrado."));

        entity.setSenhaHash(senhaHash);

        usuarioRepository.save(entity);
    }
}