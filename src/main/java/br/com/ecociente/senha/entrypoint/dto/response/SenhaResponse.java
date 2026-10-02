package br.com.ecociente.senha.entrypoint.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@Builder
@AllArgsConstructor
public class SenhaResponse {
    private Timestamp processadoEm;
}