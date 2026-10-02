package br.com.ecociente.senha.dataprovaider.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Timestamp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import br.com.ecociente.senha.core.domain.RecuperacaoSenha;
import br.com.ecociente.senha.dataprovaider.entity.EsqueceuSenhaEntity;

class RecuperarSenhaMapperTest {

    private RecuperarSenhaMapper mapper;

    private Timestamp expiraEm;
    private Timestamp utilizadoEm;

    @BeforeEach
    void setUp() {
        mapper = new RecuperarSenhaMapper();

        expiraEm = Timestamp.valueOf("2026-10-02 16:15:00");
        utilizadoEm = Timestamp.valueOf("2026-10-02 16:05:00");
    }

    @Nested
    @DisplayName("toDomain")
    class ToDomain {

        @Test
        @DisplayName("Deve preservar dados da recuperação ao converter para domain")
        void shouldMapearEntityParaDomain() {
            EsqueceuSenhaEntity entity = EsqueceuSenhaEntity.builder()
                    .id(20)
                    .usuarioId(10)
                    .tokenHash("hash-codigo")
                    .expiraEm(expiraEm)
                    .utilizadoEm(utilizadoEm)
                    .build();

            RecuperacaoSenha resultado = mapper.toDomain(entity);

            assertNotNull(resultado);
            assertEquals(20, resultado.getId());
            assertEquals(10, resultado.getUsuarioId());
            assertEquals("hash-codigo", resultado.getTokenHash());
            assertEquals(expiraEm, resultado.getExpiraEm());
            assertEquals(utilizadoEm, resultado.getUtilizadoEm());
        }
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("Deve mapear nova recuperação sem ID e sem utilização")
        void shouldMapearNovaRecuperacao() {
            RecuperacaoSenha domain = RecuperacaoSenha.builder()
                    .usuarioId(10)
                    .tokenHash("hash-codigo")
                    .expiraEm(expiraEm)
                    .build();

            EsqueceuSenhaEntity resultado = mapper.toEntity(domain);

            assertNotNull(resultado);
            assertNull(resultado.getId());
            assertEquals(10, resultado.getUsuarioId());
            assertEquals("hash-codigo", resultado.getTokenHash());
            assertEquals(expiraEm, resultado.getExpiraEm());
            assertNull(resultado.getUtilizadoEm());
        }

        @Test
        @DisplayName("Deve preservar ID e utilização ao atualizar recuperação")
        void shouldPreservarDadosNaAtualizacao() {
            RecuperacaoSenha domain = RecuperacaoSenha.builder()
                    .id(20)
                    .usuarioId(10)
                    .tokenHash("hash-codigo")
                    .expiraEm(expiraEm)
                    .utilizadoEm(utilizadoEm)
                    .build();

            EsqueceuSenhaEntity resultado = mapper.toEntity(domain);

            assertNotNull(resultado);
            assertEquals(20, resultado.getId());
            assertEquals(10, resultado.getUsuarioId());
            assertEquals("hash-codigo", resultado.getTokenHash());
            assertEquals(expiraEm, resultado.getExpiraEm());
            assertEquals(utilizadoEm, resultado.getUtilizadoEm());
        }
    }
}