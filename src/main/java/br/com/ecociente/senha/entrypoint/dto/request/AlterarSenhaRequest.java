package br.com.ecociente.senha.entrypoint.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AlterarSenhaRequest(

        @NotBlank(message = "A senha atual é obrigatória")
        String senhaAtual,

        @NotBlank(message = "A nova senha é obrigatória")
        @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S{8,100}$",
            message = "A senha deve ter no mínimo 8 caracteres, incluindo uma letra maiúscula, uma letra minúscula, um número e um caractere especial"
        )
        String novaSenha,

        @NotBlank(message = "A confirmação da senha é obrigatória")
        String confirmacaoSenha

) {
}