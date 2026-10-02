package br.com.ecociente.senha.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecuperacaoSenha {

    private Integer id;
    private Integer usuarioId;
    private String tokenHash;
    private Timestamp expiraEm;
    private Timestamp utilizadoEm;
}
