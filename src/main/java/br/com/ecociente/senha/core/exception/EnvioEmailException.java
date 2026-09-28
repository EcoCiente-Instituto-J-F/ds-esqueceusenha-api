package br.com.ecociente.senha.core.exception;

public class EnvioEmailException extends RuntimeException {

    public EnvioEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}