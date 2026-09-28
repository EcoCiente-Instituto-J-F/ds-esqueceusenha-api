package br.com.ecociente.senha.dataprovaider.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "tb_usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "nome_usuario", nullable = false, length = 100)
    private String nomeUsuario;

    @Column(name = "email_usuario", nullable = false, length = 255)
    private String emailUsuario;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(name = "cpf_cnpj", length = 18)
    private String cpfCnpj;

    @Column(name = "url_avatar", length = 500)
    private String urlAvatar;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo;

    @Column(name = "registro_em", nullable = false)
    private OffsetDateTime registroEm;

    @Column(name = "tipo_usuario_id", nullable = false)
    private Integer tipoUsuarioId;
}