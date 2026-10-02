package br.com.ecociente.senha.core.gateway;

import br.com.ecociente.senha.core.domain.RecuperacaoSenha;

import java.sql.Timestamp;
import java.util.Optional;

public interface RecuperacaoSenhaGateway {

    RecuperacaoSenha salvar(RecuperacaoSenha recuperacaoSenha);

    Optional<RecuperacaoSenha> buscarUltimaPorUsuarioId(Integer usuarioId);

    void invalidarPendentes(Integer usuarioId, Timestamp agora);
}
