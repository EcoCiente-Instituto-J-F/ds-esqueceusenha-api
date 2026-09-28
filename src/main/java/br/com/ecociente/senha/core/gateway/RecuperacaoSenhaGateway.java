package br.com.ecociente.senha.core.gateway;

import java.time.OffsetDateTime;
import java.util.Optional;

import br.com.ecociente.senha.core.domain.RecuperacaoSenha;

public interface RecuperacaoSenhaGateway {
    RecuperacaoSenha salvar(RecuperacaoSenha recuperacaoSenha);

    Optional<Integer> buscarUsuarioIdPorTokenHash(String tokenHash);

    Optional<RecuperacaoSenha> buscarPorTokenHash(String tokenHash);

    void invalidarPendentes(Integer usuarioId, OffsetDateTime agora);
}
