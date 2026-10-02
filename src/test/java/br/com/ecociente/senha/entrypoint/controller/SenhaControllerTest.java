package br.com.ecociente.senha.entrypoint.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.sql.Timestamp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.ecociente.senha.config.security.JwtAuthenticationFilter;
import br.com.ecociente.senha.config.security.JwtService;
import br.com.ecociente.senha.config.security.SecurityConfig;
import br.com.ecociente.senha.core.exception.EnvioEmailException;
import br.com.ecociente.senha.core.exception.RegraNegocioException;
import br.com.ecociente.senha.core.service.SenhaService;
import br.com.ecociente.senha.entrypoint.exception.ApiExceptionHandler;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@WebMvcTest(controllers = SenhaController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        ApiExceptionHandler.class
})
class SenhaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SenhaService senhaService;

    @MockitoBean
    private JwtService jwtService;

    private static final String EMAIL = "teste@email.com";
    private static final String CODIGO = "0427";
    private static final String NOVA_SENHA = "Nova@12345";

    private static final Timestamp PROCESSADO_EM =
            Timestamp.valueOf("2026-10-02 16:00:00");

    private static final String JSON_ESQUECEU = """
            {
              "email": "teste@email.com"
            }
            """;

    private static final String JSON_REDEFINIR = """
            {
              "email": "teste@email.com",
              "token": "0427",
              "novaSenha": "Nova@12345",
              "confirmacaoSenha": "Nova@12345"
            }
            """;

    private static final String JSON_ALTERAR = """
            {
              "senhaAtual": "Atual@12345",
              "novaSenha": "Nova@12345",
              "confirmacaoSenha": "Nova@12345"
            }
            """;

    private void prepararJwtValido() {
        Claims claims = Jwts.claims()
                .subject(EMAIL)
                .build();

        when(jwtService.isTokenValido("jwt-teste"))
                .thenReturn(true);

        when(jwtService.extrairTodosClaims("jwt-teste"))
                .thenReturn(claims);
    }

    @Nested
    @DisplayName("POST /senhas/esqueceu")
    class EsqueceuSenha {

        @Test
        @DisplayName("Deve permitir solicitação sem JWT e retornar 200")
        void shouldRetornar200SemJwt() throws Exception {
            when(senhaService.solicitarRecuperacao(EMAIL))
                    .thenReturn(PROCESSADO_EM);

            mockMvc.perform(post("/senhas/esqueceu")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_ESQUECEU))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.processadoEm").exists())
                    .andExpect(jsonPath("$.token").doesNotExist())
                    .andExpect(jsonPath("$.novaSenha").doesNotExist());

            verify(senhaService).solicitarRecuperacao(EMAIL);
        }

        @Test
        @DisplayName("Deve retornar 400 quando e-mail estiver vazio")
        void shouldRetornar400QuandoEmailVazio() throws Exception {
            mockMvc.perform(post("/senhas/esqueceu")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": ""}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigoError").value("VALIDACAO"));

            verifyNoInteractions(senhaService);
        }

        @Test
        @DisplayName("Deve retornar 400 quando e-mail for inválido")
        void shouldRetornar400QuandoEmailInvalido() throws Exception {
            mockMvc.perform(post("/senhas/esqueceu")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email": "email-invalido"}
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigoError").value("VALIDACAO"));

            verifyNoInteractions(senhaService);
        }

        @Test
        @DisplayName("Deve retornar 400 quando JSON estiver malformado")
        void shouldRetornar400QuandoJsonInvalido() throws Exception {
            mockMvc.perform(post("/senhas/esqueceu")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ json invalido"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigoError").value("VALIDACAO"));

            verifyNoInteractions(senhaService);
        }

        @Test
        @DisplayName("Deve retornar 503 quando envio de e-mail falhar")
        void shouldRetornar503QuandoEnvioFalhar() throws Exception {
            when(senhaService.solicitarRecuperacao(EMAIL))
                    .thenThrow(new EnvioEmailException(
                            "Falha de envio",
                            new RuntimeException("Erro SMTP")
                    ));

            mockMvc.perform(post("/senhas/esqueceu")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_ESQUECEU))
                    .andExpect(status().isServiceUnavailable())
                    .andExpect(jsonPath("$.codigoError")
                            .value("ENVIO_EMAIL_INDISPONIVEL"));
        }

        @Test
        @DisplayName("Deve retornar 500 sem expor detalhes internos")
        void shouldRetornar500QuandoErroInterno() throws Exception {
            when(senhaService.solicitarRecuperacao(EMAIL))
                    .thenThrow(new RuntimeException("Credencial privada"));

            mockMvc.perform(post("/senhas/esqueceu")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_ESQUECEU))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.codigoError").value("ERRO_INTERNO"))
                    .andExpect(jsonPath("$.details[0].message")
                            .value("Erro interno do servidor"));
        }
    }

    @Nested
    @DisplayName("POST /senhas/redefinir")
    class RedefinirSenha {

        @Test
        @DisplayName("Deve redefinir sem JWT e preservar zero inicial do código")
        void shouldRetornar200QuandoRedefinicaoValida() throws Exception {
            when(senhaService.redefinirSenha(
                    EMAIL, CODIGO, NOVA_SENHA, NOVA_SENHA
            )).thenReturn(PROCESSADO_EM);

            mockMvc.perform(post("/senhas/redefinir")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_REDEFINIR))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.processadoEm").exists())
                    .andExpect(jsonPath("$.token").doesNotExist())
                    .andExpect(jsonPath("$.novaSenha").doesNotExist());

            verify(senhaService).redefinirSenha(
                    EMAIL, "0427", NOVA_SENHA, NOVA_SENHA
            );
        }

        @Test
        @DisplayName("Deve retornar 400 quando e-mail estiver ausente")
        void shouldRetornar400QuandoEmailAusente() throws Exception {
            mockMvc.perform(post("/senhas/redefinir")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "token": "0427",
                                      "novaSenha": "Nova@12345",
                                      "confirmacaoSenha": "Nova@12345"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigoError").value("VALIDACAO"));

            verifyNoInteractions(senhaService);
        }

        @Test
        @DisplayName("Deve retornar 400 quando código não tiver quatro dígitos")
        void shouldRetornar400QuandoCodigoInvalido() throws Exception {
            mockMvc.perform(post("/senhas/redefinir")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_REDEFINIR.replace("0427", "12345")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigoError").value("VALIDACAO"));

            verifyNoInteractions(senhaService);
        }

        @Test
        @DisplayName("Deve retornar 400 quando confirmação estiver ausente")
        void shouldRetornar400QuandoConfirmacaoAusente() throws Exception {
            mockMvc.perform(post("/senhas/redefinir")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "email": "teste@email.com",
                                      "token": "0427",
                                      "novaSenha": "Nova@12345"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigoError").value("VALIDACAO"));

            verifyNoInteractions(senhaService);
        }

        @Test
        @DisplayName("Deve retornar 400 quando senha for fraca")
        void shouldRetornar400QuandoSenhaFraca() throws Exception {
            mockMvc.perform(post("/senhas/redefinir")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_REDEFINIR.replace("Nova@12345", "123")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigoError").value("VALIDACAO"));

            verifyNoInteractions(senhaService);
        }

        @Test
        @DisplayName("Deve retornar 400 quando service rejeitar o código")
        void shouldRetornar400QuandoCodigoRejeitado() throws Exception {
            when(senhaService.redefinirSenha(
                    EMAIL, CODIGO, NOVA_SENHA, NOVA_SENHA
            )).thenThrow(new RegraNegocioException(
                    "Código inválido, expirado ou já utilizado."
            ));

            mockMvc.perform(post("/senhas/redefinir")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_REDEFINIR))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigoError").value("REGRA_NEGOCIO"))
                    .andExpect(jsonPath("$.details[0].message")
                            .value("Código inválido, expirado ou já utilizado."));
        }
    }

    @Nested
    @DisplayName("POST /senhas/alterar")
    class AlterarSenha {

        @Test
        @DisplayName("Deve usar o e-mail extraído do JWT para alterar a senha")
        void shouldAlterarSenhaDoUsuarioAutenticado() throws Exception {
            prepararJwtValido();

            when(senhaService.alterarSenha(
                    EMAIL, "Atual@12345", NOVA_SENHA, NOVA_SENHA
            )).thenReturn(PROCESSADO_EM);

            mockMvc.perform(post("/senhas/alterar")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer jwt-teste")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_ALTERAR))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.processadoEm").exists());

            verify(senhaService).alterarSenha(
                    EMAIL, "Atual@12345", NOVA_SENHA, NOVA_SENHA
            );
        }

        @Test
        @DisplayName("Deve retornar 401 quando JWT não for enviado")
        void shouldRetornar401SemJwt() throws Exception {
            mockMvc.perform(post("/senhas/alterar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_ALTERAR))
                    .andExpect(status().isUnauthorized());

            verifyNoInteractions(senhaService);
        }

        @Test
        @DisplayName("Deve retornar 401 quando JWT for inválido")
        void shouldRetornar401QuandoJwtInvalido() throws Exception {
            when(jwtService.isTokenValido("jwt-invalido"))
                    .thenReturn(false);

            mockMvc.perform(post("/senhas/alterar")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer jwt-invalido")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_ALTERAR))
                    .andExpect(status().isUnauthorized());

            verifyNoInteractions(senhaService);
        }

        @Test
        @DisplayName("Deve retornar 400 quando senha atual estiver ausente")
        void shouldRetornar400QuandoSenhaAtualAusente() throws Exception {
            prepararJwtValido();

            mockMvc.perform(post("/senhas/alterar")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer jwt-teste")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "novaSenha": "Nova@12345",
                                      "confirmacaoSenha": "Nova@12345"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigoError").value("VALIDACAO"));

            verifyNoInteractions(senhaService);
        }

        @Test
        @DisplayName("Deve retornar 400 quando senha atual estiver incorreta")
        void shouldRetornar400QuandoSenhaAtualIncorreta() throws Exception {
            prepararJwtValido();

            when(senhaService.alterarSenha(
                    EMAIL, "Atual@12345", NOVA_SENHA, NOVA_SENHA
            )).thenThrow(new RegraNegocioException(
                    "A senha atual está incorreta."
            ));

            mockMvc.perform(post("/senhas/alterar")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer jwt-teste")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON_ALTERAR))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.codigoError").value("REGRA_NEGOCIO"));
        }
    }
}