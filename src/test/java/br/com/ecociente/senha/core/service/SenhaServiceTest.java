package br.com.ecociente.senha.core.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.ecociente.senha.core.domain.RecuperacaoSenha;
import br.com.ecociente.senha.core.domain.Usuario;
import br.com.ecociente.senha.core.exception.EnvioEmailException;
import br.com.ecociente.senha.core.exception.RecursoNaoEncontradoException;
import br.com.ecociente.senha.core.exception.RegraNegocioException;
import br.com.ecociente.senha.core.gateway.EmailGateway;
import br.com.ecociente.senha.core.gateway.RecuperacaoSenhaGateway;
import br.com.ecociente.senha.core.gateway.SenhaEncoderGateway;
import br.com.ecociente.senha.core.gateway.UsuarioGateway;

@ExtendWith(MockitoExtension.class)
class SenhaServiceTest {

    @Mock
    private UsuarioGateway usuarioGateway;

    @Mock
    private RecuperacaoSenhaGateway recuperacaoSenhaGateway;

    @Mock
    private SenhaEncoderGateway senhaEncoderGateway;

    @Mock
    private EmailGateway emailGateway;

    @InjectMocks
    private SenhaService service;

    private static final String EMAIL = "teste@email.com";
    private static final String CODIGO = "0427";
    private static final String SENHA_ATUAL = "Atual@12345";
    private static final String NOVA_SENHA = "Nova@12345";
    private static final String HASH_ATUAL = "hash-senha-atual";
    private static final String HASH_NOVO = "hash-nova-senha";
    private static final String HASH_CODIGO = "hash-codigo";

    private Usuario usuario;
    private RecuperacaoSenha recuperacao;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(10)
                .email(EMAIL)
                .senha(HASH_ATUAL)
                .build();

        recuperacao = RecuperacaoSenha.builder()
                .id(20)
                .usuarioId(10)
                .tokenHash(HASH_CODIGO)
                .expiraEm(new Timestamp(
                        System.currentTimeMillis()
                                + TimeUnit.MINUTES.toMillis(15)
                ))
                .utilizadoEm(null)
                .build();
    }

    private void prepararBuscaRecuperacao() {
        when(usuarioGateway.buscarPorEmail(EMAIL))
                .thenReturn(Optional.of(usuario));

        when(recuperacaoSenhaGateway.buscarUltimaPorUsuarioId(10))
                .thenReturn(Optional.of(recuperacao));
    }

    private void verificarSemAlteracaoDeSenha() {
        verify(usuarioGateway, never())
                .atualizarSenha(anyInt(), anyString());

        verify(recuperacaoSenhaGateway, never())
                .salvar(any(RecuperacaoSenha.class));

        verify(recuperacaoSenhaGateway, never())
                .invalidarPendentes(anyInt(), any(Timestamp.class));
    }

    @Nested
    @DisplayName("solicitarRecuperacao")
    class SolicitarRecuperacao {

        @Test
        @DisplayName("Deve enviar código de quatro dígitos e salvar somente seu hash")
        void shouldSolicitarRecuperacaoComSucesso() {
            when(usuarioGateway.buscarPorEmail(EMAIL))
                    .thenReturn(Optional.of(usuario));

            when(senhaEncoderGateway.gerarHash(anyString()))
                    .thenReturn(HASH_CODIGO);

            long antes = System.currentTimeMillis();

            Timestamp resultado = service.solicitarRecuperacao(EMAIL);

            long depois = System.currentTimeMillis();

            ArgumentCaptor<String> codigoCaptor =
                    ArgumentCaptor.forClass(String.class);

            ArgumentCaptor<RecuperacaoSenha> recuperacaoCaptor =
                    ArgumentCaptor.forClass(RecuperacaoSenha.class);

            verify(senhaEncoderGateway).gerarHash(codigoCaptor.capture());
            verify(recuperacaoSenhaGateway).salvar(recuperacaoCaptor.capture());

            String codigo = codigoCaptor.getValue();
            RecuperacaoSenha salva = recuperacaoCaptor.getValue();

            assertNotNull(resultado);
            assertTrue(codigo.matches("[0-9]{4}"));
            assertEquals(10, salva.getUsuarioId());
            assertEquals(HASH_CODIGO, salva.getTokenHash());
            assertNotEquals(codigo, salva.getTokenHash());
            assertNull(salva.getUtilizadoEm());

            long validade = TimeUnit.MINUTES.toMillis(15);

            assertTrue(salva.getExpiraEm().getTime() >= antes + validade);
            assertTrue(salva.getExpiraEm().getTime() <= depois + validade);

            verify(emailGateway).enviarRecuperacao(EMAIL, codigo);

            var ordem = inOrder(recuperacaoSenhaGateway, emailGateway);

            ordem.verify(recuperacaoSenhaGateway)
                    .invalidarPendentes(eq(10), any(Timestamp.class));

            ordem.verify(recuperacaoSenhaGateway)
                    .salvar(any(RecuperacaoSenha.class));

            ordem.verify(emailGateway).enviarRecuperacao(EMAIL, codigo);
        }

        @Test
        @DisplayName("Deve retornar normalmente quando e-mail não estiver cadastrado")
        void shouldNaoRevelarEmailInexistente() {
            when(usuarioGateway.buscarPorEmail(EMAIL))
                    .thenReturn(Optional.empty());

            Timestamp resultado = service.solicitarRecuperacao(EMAIL);

            assertNotNull(resultado);

            verifyNoInteractions(
                    recuperacaoSenhaGateway,
                    senhaEncoderGateway,
                    emailGateway
            );
        }

        @Test
        @DisplayName("Deve propagar falha no envio do e-mail")
        void shouldPropagarErroDeEmail() {
            when(usuarioGateway.buscarPorEmail(EMAIL))
                    .thenReturn(Optional.of(usuario));

            when(senhaEncoderGateway.gerarHash(anyString()))
                    .thenReturn(HASH_CODIGO);

            EnvioEmailException falha = new EnvioEmailException(
                    "Falha no envio",
                    new RuntimeException("SMTP indisponível")
            );

            doThrow(falha).when(emailGateway)
                    .enviarRecuperacao(eq(EMAIL), anyString());

            EnvioEmailException resultado = assertThrows(
                    EnvioEmailException.class,
                    () -> service.solicitarRecuperacao(EMAIL)
            );

            assertSame(falha, resultado);
        }
    }

    @Nested
    @DisplayName("redefinirSenha")
    class RedefinirSenha {

        @Test
        @DisplayName("Deve redefinir senha com código válido, preservando zero inicial")
        void shouldRedefinirSenhaComSucesso() {
            prepararBuscaRecuperacao();

            when(senhaEncoderGateway.corresponde(CODIGO, HASH_CODIGO))
                    .thenReturn(true);

            when(senhaEncoderGateway.gerarHash(NOVA_SENHA))
                    .thenReturn(HASH_NOVO);

            Timestamp resultado = service.redefinirSenha(
                    EMAIL, CODIGO, NOVA_SENHA, NOVA_SENHA
            );

            assertNotNull(resultado);

            verify(senhaEncoderGateway).corresponde("0427", HASH_CODIGO);
            verify(usuarioGateway).atualizarSenha(10, HASH_NOVO);

            ArgumentCaptor<RecuperacaoSenha> captor =
                    ArgumentCaptor.forClass(RecuperacaoSenha.class);

            verify(recuperacaoSenhaGateway).salvar(captor.capture());

            assertEquals(20, captor.getValue().getId());
            assertEquals(10, captor.getValue().getUsuarioId());
            assertNotNull(captor.getValue().getUtilizadoEm());

            verify(recuperacaoSenhaGateway)
                    .invalidarPendentes(eq(10), any(Timestamp.class));

            verifyNoInteractions(emailGateway);
        }

        @Test
        @DisplayName("Deve rejeitar confirmação diferente da nova senha")
        void shouldRejeitarConfirmacaoDiferente() {
            RegraNegocioException exception = assertThrows(
                    RegraNegocioException.class,
                    () -> service.redefinirSenha(
                            EMAIL, CODIGO, NOVA_SENHA, "Outra@12345"
                    )
            );

            assertEquals(
                    "A nova senha e a confirmação devem ser iguais.",
                    exception.getMessage()
            );

            verifyNoInteractions(
                    usuarioGateway,
                    recuperacaoSenhaGateway,
                    senhaEncoderGateway,
                    emailGateway
            );
        }

        @Test
        @DisplayName("Deve rejeitar senha fora do padrão")
        void shouldRejeitarSenhaFraca() {
            assertThrows(
                    RegraNegocioException.class,
                    () -> service.redefinirSenha(
                            EMAIL, CODIGO, "123", "123"
                    )
            );

            verifyNoInteractions(
                    usuarioGateway,
                    recuperacaoSenhaGateway,
                    senhaEncoderGateway
            );
        }

        @Test
        @DisplayName("Deve rejeitar códigos sem exatamente quatro dígitos")
        void shouldRejeitarFormatoInvalido() {
            for (String codigo : new String[]{null, "", "123", "12345", "12a4"}) {
                assertThrows(
                        RegraNegocioException.class,
                        () -> service.redefinirSenha(
                                EMAIL, codigo, NOVA_SENHA, NOVA_SENHA
                        )
                );
            }

            verifyNoInteractions(
                    usuarioGateway,
                    recuperacaoSenhaGateway,
                    senhaEncoderGateway
            );
        }

        @Test
        @DisplayName("Deve rejeitar e-mail não encontrado sem alterar senha")
        void shouldRejeitarEmailInexistente() {
            when(usuarioGateway.buscarPorEmail(EMAIL))
                    .thenReturn(Optional.empty());

            assertThrows(
                    RegraNegocioException.class,
                    () -> service.redefinirSenha(
                            EMAIL, CODIGO, NOVA_SENHA, NOVA_SENHA
                    )
            );

            verifyNoInteractions(
                    recuperacaoSenhaGateway,
                    senhaEncoderGateway
            );

            verify(usuarioGateway, never())
                    .atualizarSenha(anyInt(), anyString());
        }

        @Test
        @DisplayName("Deve rejeitar usuário sem recuperação cadastrada")
        void shouldRejeitarRecuperacaoInexistente() {
            when(usuarioGateway.buscarPorEmail(EMAIL))
                    .thenReturn(Optional.of(usuario));

            when(recuperacaoSenhaGateway.buscarUltimaPorUsuarioId(10))
                    .thenReturn(Optional.empty());

            assertThrows(
                    RegraNegocioException.class,
                    () -> service.redefinirSenha(
                            EMAIL, CODIGO, NOVA_SENHA, NOVA_SENHA
                    )
            );

            verificarSemAlteracaoDeSenha();
            verifyNoInteractions(senhaEncoderGateway);
        }

        @Test
        @DisplayName("Deve rejeitar código expirado")
        void shouldRejeitarCodigoExpirado() {
            recuperacao.setExpiraEm(
                    new Timestamp(System.currentTimeMillis() - 60_000)
            );

            prepararBuscaRecuperacao();

            assertThrows(
                    RegraNegocioException.class,
                    () -> service.redefinirSenha(
                            EMAIL, CODIGO, NOVA_SENHA, NOVA_SENHA
                    )
            );

            verificarSemAlteracaoDeSenha();
            verifyNoInteractions(senhaEncoderGateway);
        }

        @Test
        @DisplayName("Deve rejeitar código já utilizado")
        void shouldRejeitarCodigoUtilizado() {
            recuperacao.setUtilizadoEm(
                    new Timestamp(System.currentTimeMillis() - 1_000)
            );

            prepararBuscaRecuperacao();

            assertThrows(
                    RegraNegocioException.class,
                    () -> service.redefinirSenha(
                            EMAIL, CODIGO, NOVA_SENHA, NOVA_SENHA
                    )
            );

            verificarSemAlteracaoDeSenha();
            verifyNoInteractions(senhaEncoderGateway);
        }

        @Test
        @DisplayName("Deve rejeitar código incorreto sem marcar recuperação como utilizada")
        void shouldRejeitarCodigoIncorreto() {
            prepararBuscaRecuperacao();

            when(senhaEncoderGateway.corresponde(CODIGO, HASH_CODIGO))
                    .thenReturn(false);

            assertThrows(
                    RegraNegocioException.class,
                    () -> service.redefinirSenha(
                            EMAIL, CODIGO, NOVA_SENHA, NOVA_SENHA
                    )
            );

            assertNull(recuperacao.getUtilizadoEm());
            verificarSemAlteracaoDeSenha();

            verify(senhaEncoderGateway, never()).gerarHash(anyString());
        }

        @Test
        @DisplayName("Deve consultar somente a recuperação do e-mail informado")
        void shouldConsultarRecuperacaoDoUsuarioInformado() {
            Usuario outroUsuario = Usuario.builder()
                    .id(99)
                    .email("outro@email.com")
                    .senha("outro-hash")
                    .build();

            when(usuarioGateway.buscarPorEmail("outro@email.com"))
                    .thenReturn(Optional.of(outroUsuario));

            when(recuperacaoSenhaGateway.buscarUltimaPorUsuarioId(99))
                    .thenReturn(Optional.empty());

            assertThrows(
                    RegraNegocioException.class,
                    () -> service.redefinirSenha(
                            "outro@email.com", CODIGO, NOVA_SENHA, NOVA_SENHA
                    )
            );

            verify(recuperacaoSenhaGateway).buscarUltimaPorUsuarioId(99);
            verify(recuperacaoSenhaGateway, never()).buscarUltimaPorUsuarioId(10);

            verificarSemAlteracaoDeSenha();
        }
    }

    @Nested
    @DisplayName("alterarSenha")
    class AlterarSenha {

        @Test
        @DisplayName("Deve alterar senha quando a senha atual estiver correta")
        void shouldAlterarSenhaComSucesso() {
            when(usuarioGateway.buscarPorEmail(EMAIL))
                    .thenReturn(Optional.of(usuario));

            when(senhaEncoderGateway.corresponde(SENHA_ATUAL, HASH_ATUAL))
                    .thenReturn(true);

            when(senhaEncoderGateway.gerarHash(NOVA_SENHA))
                    .thenReturn(HASH_NOVO);

            Timestamp resultado = service.alterarSenha(
                    EMAIL, SENHA_ATUAL, NOVA_SENHA, NOVA_SENHA
            );

            assertNotNull(resultado);

            verify(usuarioGateway).atualizarSenha(10, HASH_NOVO);

            verify(recuperacaoSenhaGateway)
                    .invalidarPendentes(eq(10), any(Timestamp.class));
        }

        @Test
        @DisplayName("Deve rejeitar senha atual incorreta")
        void shouldRejeitarSenhaAtualIncorreta() {
            when(usuarioGateway.buscarPorEmail(EMAIL))
                    .thenReturn(Optional.of(usuario));

            when(senhaEncoderGateway.corresponde(SENHA_ATUAL, HASH_ATUAL))
                    .thenReturn(false);

            RegraNegocioException exception = assertThrows(
                    RegraNegocioException.class,
                    () -> service.alterarSenha(
                            EMAIL, SENHA_ATUAL, NOVA_SENHA, NOVA_SENHA
                    )
            );

            assertEquals(
                    "A senha atual está incorreta.",
                    exception.getMessage()
            );

            verificarSemAlteracaoDeSenha();
            verify(senhaEncoderGateway, never()).gerarHash(anyString());
        }

        @Test
        @DisplayName("Deve rejeitar alteração para usuário não encontrado")
        void shouldRejeitarUsuarioInexistente() {
            when(usuarioGateway.buscarPorEmail(EMAIL))
                    .thenReturn(Optional.empty());

            assertThrows(
                    RecursoNaoEncontradoException.class,
                    () -> service.alterarSenha(
                            EMAIL, SENHA_ATUAL, NOVA_SENHA, NOVA_SENHA
                    )
            );

            verificarSemAlteracaoDeSenha();
            verifyNoInteractions(senhaEncoderGateway);
        }

        @Test
        @DisplayName("Deve rejeitar alteração com confirmação diferente")
        void shouldRejeitarConfirmacaoDiferente() {
            assertThrows(
                    RegraNegocioException.class,
                    () -> service.alterarSenha(
                            EMAIL, SENHA_ATUAL, NOVA_SENHA, "Outra@12345"
                    )
            );

            verifyNoInteractions(
                    usuarioGateway,
                    recuperacaoSenhaGateway,
                    senhaEncoderGateway
            );
        }
    }
}