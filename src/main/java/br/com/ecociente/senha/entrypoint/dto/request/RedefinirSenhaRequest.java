package br.com.ecociente.senha.entrypoint.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RedefinirSenhaRequest(

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Informe um e-mail válido")
        String email,

        @NotBlank(message = "O código é obrigatório")
        @Pattern(
                regexp = "^[0-9]{4}$",
                message = "O código deve conter exatamente 4 dígitos"
        )
        String token,

        @NotBlank(message = "A nova senha é obrigatória")
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S{8,100}$",
                message = "A senha deve ter entre 8 e 100 caracteres, incluindo maiúscula, minúscula, número e caractere especial"
        )
        String novaSenha,

        @NotBlank(message = "A confirmação da senha é obrigatória")
        String confirmacaoSenha

) {
}