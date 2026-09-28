package br.com.ecociente.senha.entrypoint.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ValidationError {

    private String field;
    private String message;
}