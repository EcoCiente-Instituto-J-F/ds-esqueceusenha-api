package br.com.ecociente.senha.dataprovaider.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import br.com.ecociente.senha.dataprovaider.entity.EsqueceuSenhaEntity;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;

public interface EsqueceuSenhaRepositoy
        extends JpaRepository<EsqueceuSenhaEntity, Integer> {

    @Query("""
            select r.usuarioId
            from EsqueceuSenhaEntity r
            where r.tokenHash = :tokenHash
            """)
    Optional<Integer> buscarUsuarioIdPorTokenHash(
            @Param("tokenHash") String tokenHash
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<EsqueceuSenhaEntity> findByTokenHash(String tokenHash);

    @Modifying(flushAutomatically = true)
    @Query("""
            update EsqueceuSenhaEntity r
            set r.expiraEm = :agora
            where r.usuarioId = :usuarioId
              and r.utilizadoEm is null
              and r.expiraEm > :agora
            """)
    int invalidarPendentes(
            @Param("usuarioId") Integer usuarioId,
            @Param("agora") OffsetDateTime agora
    );
}
