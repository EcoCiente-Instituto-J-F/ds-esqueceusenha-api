package br.com.ecociente.senha.dataprovaider.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "tb_esqueceuSenha")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EsqueceuSenhaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recuperacao")
    private Integer id;

    @Column(name = "id_usuario", nullable = false)
    private Integer usuarioId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expira_em", nullable = false)
    private OffsetDateTime expiraEm;

    @Column(name = "utilizado_em")
    private OffsetDateTime utilizadoEm;
  
}
