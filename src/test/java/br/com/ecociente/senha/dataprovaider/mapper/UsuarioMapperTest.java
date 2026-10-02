package br.com.ecociente.senha.dataprovaider.mapper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.ecociente.senha.core.domain.Usuario;
import br.com.ecociente.senha.dataprovaider.entity.UsuarioEntity;

class UsuarioMapperTest {

    private UsuarioMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UsuarioMapper();
    }

    @Test
    @DisplayName("Deve mapear identificador, e-mail e hash da senha para a domain")
    void shouldMapearEntityParaDomain() {
        UsuarioEntity entity = UsuarioEntity.builder()
                .idUsuario(10)
                .emailUsuario("teste@email.com")
                .senhaHash("hash-senha")
                .build();

        Usuario resultado = mapper.toDomain(entity);

        assertNotNull(resultado);
        assertEquals(10, resultado.getId());
        assertEquals("teste@email.com", resultado.getEmail());
        assertEquals("hash-senha", resultado.getSenha());
    }
}