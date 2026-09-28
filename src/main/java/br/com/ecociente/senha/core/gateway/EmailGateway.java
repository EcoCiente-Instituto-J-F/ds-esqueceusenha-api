package br.com.ecociente.senha.core.gateway;

public interface EmailGateway {

    void enviarRecuperacao(String destinatario, String token);
}