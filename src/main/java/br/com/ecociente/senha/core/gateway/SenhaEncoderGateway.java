package br.com.ecociente.senha.core.gateway;

public interface SenhaEncoderGateway {

    String gerarHash(String senha);

    boolean corresponde(String senha, String senhaHash);
}