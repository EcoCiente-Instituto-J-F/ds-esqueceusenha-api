package br.com.ecociente.senha.entrypoint.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {

    private Integer status;
    private String codigoError;
    private List<ValidationError> details;
}