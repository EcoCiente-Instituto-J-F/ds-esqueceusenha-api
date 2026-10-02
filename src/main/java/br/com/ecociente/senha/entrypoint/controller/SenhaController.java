package br.com.ecociente.senha.entrypoint.controller;

import br.com.ecociente.senha.core.service.SenhaService;
import br.com.ecociente.senha.entrypoint.dto.request.AlterarSenhaRequest;
import br.com.ecociente.senha.entrypoint.dto.request.EsqueciSenhaRequest;
import br.com.ecociente.senha.entrypoint.dto.request.RedefinirSenhaRequest;
import br.com.ecociente.senha.entrypoint.dto.response.SenhaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;

@RestController
@RequestMapping("/senhas")
@RequiredArgsConstructor
public class SenhaController {

    private final SenhaService senhaService;

    @PostMapping("/esqueceu")
    public ResponseEntity<SenhaResponse> esqueceuSenha(
            @Valid @RequestBody EsqueciSenhaRequest request
    ) {
        Timestamp processadoEm = senhaService.solicitarRecuperacao(
                request.email()
        );

        return ResponseEntity.ok(
                SenhaResponse.builder()
                        .processadoEm(processadoEm)
                        .build()
        );
    }

    @PostMapping("/redefinir")
public ResponseEntity<SenhaResponse> redefinirSenha(
        @Valid @RequestBody RedefinirSenhaRequest request
) {
    Timestamp processadoEm = senhaService.redefinirSenha(
            request.email(),
            request.token(),
            request.novaSenha(),
            request.confirmacaoSenha()
    );

    return ResponseEntity.ok(
            SenhaResponse.builder()
                    .processadoEm(processadoEm)
                    .build()
    );
}

    @PostMapping("/alterar")
    public ResponseEntity<SenhaResponse> alterarSenha(
            @Valid @RequestBody AlterarSenhaRequest request,
            Authentication authentication
    ) {
        Timestamp processadoEm = senhaService.alterarSenha(
                authentication.getName(),
                request.senhaAtual(),
                request.novaSenha(),
                request.confirmacaoSenha()
        );

        return ResponseEntity.ok(
                SenhaResponse.builder()
                        .processadoEm(processadoEm)
                        .build()
        );
    }
}