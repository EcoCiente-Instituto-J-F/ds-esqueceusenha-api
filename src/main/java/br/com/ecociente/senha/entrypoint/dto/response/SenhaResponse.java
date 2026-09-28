package br.com.ecociente.senha.entrypoint.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
@AllArgsConstructor
public class SenhaResponse {
    private OffsetDateTime processadoEm;
}