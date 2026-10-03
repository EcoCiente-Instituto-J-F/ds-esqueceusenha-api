package br.com.ecociente.senha.core.gateway;

public interface FirebaseSenhaGateway {

    void atualizarSenha(String email, String novaSenha);
}
