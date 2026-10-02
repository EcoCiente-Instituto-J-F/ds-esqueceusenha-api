package br.com.ecociente.senha.dataprovaider.gateway;

import br.com.ecociente.senha.core.domain.RecuperacaoSenha;
import br.com.ecociente.senha.core.gateway.RecuperacaoSenhaGateway;
import br.com.ecociente.senha.dataprovaider.entity.EsqueceuSenhaEntity;
import br.com.ecociente.senha.dataprovaider.mapper.RecuperarSenhaMapper;
import br.com.ecociente.senha.dataprovaider.repository.EsqueceuSenhaRepositoy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RecuperarSenhaGatewayImpl
        implements RecuperacaoSenhaGateway {

    private final EsqueceuSenhaRepositoy esqueciSenhaRepository;
    private final RecuperarSenhaMapper recuperarSenhaMapper;

    @Override
    public RecuperacaoSenha salvar(RecuperacaoSenha recuperacaoSenha) {
        EsqueceuSenhaEntity entity =
                recuperarSenhaMapper.toEntity(recuperacaoSenha);

        EsqueceuSenhaEntity entitySalva =
                esqueciSenhaRepository.save(entity);

        return recuperarSenhaMapper.toDomain(entitySalva);
    }

    @Override
    public Optional<RecuperacaoSenha> buscarUltimaPorUsuarioId(
            Integer usuarioId
    ) {
        return esqueciSenhaRepository
                .findFirstByUsuarioIdOrderByIdDesc(usuarioId)
                .map(recuperarSenhaMapper::toDomain);
    }

    @Override
    public void invalidarPendentes(
            Integer usuarioId,
            Timestamp agora
    ) {
        esqueciSenhaRepository.invalidarPendentes(usuarioId, agora);
    }
}