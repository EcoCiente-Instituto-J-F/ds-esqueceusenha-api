package br.com.ecociente.senha.entrypoint.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import br.com.ecociente.senha.core.exception.EnvioEmailException;
import br.com.ecociente.senha.core.exception.RecursoNaoEncontradoException;
import br.com.ecociente.senha.core.exception.RegraNegocioException;

class ApiExceptionHandlerTest {

    private ApiExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApiExceptionHandler();
    }

    @Test
    @DisplayName("Deve retornar 400 quando ocorrer regra de negócio")
    void shouldRetornar400QuandoRegraNegocio() {
        var response = handler.handleRegraNegocio(
                new RegraNegocioException("As senhas devem ser iguais.")
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("REGRA_NEGOCIO", response.getBody().getCodigoError());

        assertEquals(
                "As senhas devem ser iguais.",
                response.getBody().getDetails().get(0).getMessage()
        );
    }

    @Test
    @DisplayName("Deve retornar 404 quando recurso não for encontrado")
    void shouldRetornar404QuandoRecursoNaoEncontrado() {
        var response = handler.handleNaoEncontrado(
                new RecursoNaoEncontradoException("Usuário não encontrado.")
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals(
                "RECURSO_NAO_ENCONTRADO",
                response.getBody().getCodigoError()
        );
    }

    @Test
    @DisplayName("Deve retornar 400 com os campos que falharam na validação")
    void shouldRetornarErrosDeValidacao() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);

        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError(
                        "request", "email", "O e-mail é obrigatório"
                ),
                new FieldError(
                        "request", "token", "O código é obrigatório"
                )
        ));

        var response = handler.handleValidation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertEquals("VALIDACAO", response.getBody().getCodigoError());
        assertEquals(2, response.getBody().getDetails().size());

        assertEquals(
                "email",
                response.getBody().getDetails().get(0).getField()
        );

        assertEquals(
                "O e-mail é obrigatório",
                response.getBody().getDetails().get(0).getMessage()
        );

        assertEquals(
                "token",
                response.getBody().getDetails().get(1).getField()
        );
    }

    @Test
    @DisplayName("Deve retornar 400 quando JSON estiver malformado")
    void shouldRetornar400QuandoJsonInvalido() {
        var exception = mock(HttpMessageNotReadableException.class);

        var response = handler.handleJsonInvalido(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("VALIDACAO", response.getBody().getCodigoError());

        assertEquals(
                "JSON inválido",
                response.getBody().getDetails().get(0).getMessage()
        );
    }

    @Test
    @DisplayName("Deve retornar 503 sem expor detalhes do servidor de e-mail")
    void shouldRetornar503QuandoEnvioEmailFalhar() {
        var exception = new EnvioEmailException(
                "Credencial SMTP privada",
                new RuntimeException("password=segredo")
        );

        var response = handler.handleEnvioEmail(exception);

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());
        assertEquals(503, response.getBody().getStatus());
        assertEquals(
                "ENVIO_EMAIL_INDISPONIVEL",
                response.getBody().getCodigoError()
        );

        String mensagem =
                response.getBody().getDetails().get(0).getMessage();

        assertEquals(
                "Não foi possível processar a solicitação. "
                        + "Tente novamente mais tarde.",
                mensagem
        );

        assertFalse(mensagem.contains("segredo"));
    }

    @Test
    @DisplayName("Deve retornar 500 sem expor a mensagem interna da exception")
    void shouldRetornar500SemExporDetalhesInternos() {
        var response = handler.handleGenerico(
                new RuntimeException("Credencial privada do banco")
        );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("ERRO_INTERNO", response.getBody().getCodigoError());

        assertEquals(
                "Erro interno do servidor",
                response.getBody().getDetails().get(0).getMessage()
        );
    }
}