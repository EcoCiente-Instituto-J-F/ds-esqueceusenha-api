package br.com.ecociente.senha.dataprovaider.mapper;

import br.com.ecociente.senha.core.domain.RecuperacaoSenha;
import br.com.ecociente.senha.dataprovaider.entity.EsqueceuSenhaEntity;

import org.springframework.stereotype.Component;

@Component
public class RecuperarSenhaMapper {

    public RecuperacaoSenha toDomain(EsqueceuSenhaEntity entity) {
        return RecuperacaoSenha.builder()
                .id(entity.getId())
                .usuarioId(entity.getUsuarioId())
                .tokenHash(entity.getTokenHash())
                .expiraEm(entity.getExpiraEm())
                .utilizadoEm(entity.getUtilizadoEm())
                .build();
    }

    public EsqueceuSenhaEntity toEntity(RecuperacaoSenha domain) {
        return EsqueceuSenhaEntity.builder()
                .id(domain.getId())
                .usuarioId(domain.getUsuarioId())
                .tokenHash(domain.getTokenHash())
                .expiraEm(domain.getExpiraEm())
                .utilizadoEm(domain.getUtilizadoEm())
                .build();
    }
}