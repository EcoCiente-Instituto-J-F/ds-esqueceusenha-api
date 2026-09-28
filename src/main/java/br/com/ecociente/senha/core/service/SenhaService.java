package br.com.ecociente.senha.core.service;

import br.com.ecociente.senha.core.domain.RecuperacaoSenha;
import br.com.ecociente.senha.core.domain.Usuario;
import br.com.ecociente.senha.core.exception.RecursoNaoEncontradoException;
import br.com.ecociente.senha.core.exception.RegraNegocioException;
import br.com.ecociente.senha.core.gateway.EmailGateway;
import br.com.ecociente.senha.core.gateway.RecuperacaoSenhaGateway;
import br.com.ecociente.senha.core.gateway.SenhaEncoderGateway;
import br.com.ecociente.senha.core.gateway.UsuarioGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class SenhaService {

    private static final int VALIDADE_TOKEN_MINUTOS = 15;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final Pattern PADRAO_SENHA = Pattern.compile(
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S{8,100}$"
    );

    private static final Pattern PADRAO_TOKEN = Pattern.compile(
            "^[A-Za-z0-9_-]{43}$"
    );

    private final UsuarioGateway usuarioGateway;
    private final RecuperacaoSenhaGateway recuperacaoSenhaGateway;
    private final SenhaEncoderGateway senhaEncoderGateway;
    private final EmailGateway emailGateway;

    @Transactional
    public OffsetDateTime solicitarRecuperacao(String email) {
        var usuarioOptional = usuarioGateway.buscarPorEmail(email);

        if (usuarioOptional.isEmpty()) {
            return agora();
        }

        Usuario usuario = usuarioOptional.get();
        OffsetDateTime momento = agora();

        recuperacaoSenhaGateway.invalidarPendentes(
                usuario.getId(),
                momento
        );

        String token = gerarToken();

        RecuperacaoSenha recuperacao = RecuperacaoSenha.builder()
                .usuarioId(usuario.getId())
                .tokenHash(gerarHashToken(token))
                .expiraEm(momento.plusMinutes(VALIDADE_TOKEN_MINUTOS))
                .utilizadoEm(null)
                .build();

        recuperacaoSenhaGateway.salvar(recuperacao);

        emailGateway.enviarRecuperacao(usuario.getEmail(), token);

        return agora();
    }

    @Transactional
    public OffsetDateTime redefinirSenha(
            String token,
            String novaSenha,
            String confirmacaoSenha
    ) {
        validarNovaSenha(novaSenha, confirmacaoSenha);

        if (token == null || !PADRAO_TOKEN.matcher(token).matches()) {
            throw tokenInvalido();
        }

        String tokenHash = gerarHashToken(token);

        Integer usuarioId = recuperacaoSenhaGateway
                .buscarUsuarioIdPorTokenHash(tokenHash)
                .orElseThrow(this::tokenInvalido);

        Usuario usuario = usuarioGateway.buscarPorId(usuarioId)
                .orElseThrow(this::tokenInvalido);

        RecuperacaoSenha recuperacao = recuperacaoSenhaGateway
                .buscarPorTokenHash(tokenHash)
                .orElseThrow(this::tokenInvalido);

        OffsetDateTime momento = agora();

        if (!usuario.getId().equals(recuperacao.getUsuarioId())
                || recuperacao.getUtilizadoEm() != null
                || !recuperacao.getExpiraEm().isAfter(momento)) {
            throw tokenInvalido();
        }

        String senhaHash = senhaEncoderGateway.gerarHash(novaSenha);

        usuarioGateway.atualizarSenha(usuario.getId(), senhaHash);

        recuperacao.setUtilizadoEm(momento);
        recuperacaoSenhaGateway.salvar(recuperacao);

        recuperacaoSenhaGateway.invalidarPendentes(
                usuario.getId(),
                momento
        );

        return agora();
    }

    @Transactional
    public OffsetDateTime alterarSenha(
            String emailAutenticado,
            String senhaAtual,
            String novaSenha,
            String confirmacaoSenha
    ) {
        validarNovaSenha(novaSenha, confirmacaoSenha);

        Usuario usuario = usuarioGateway
                .buscarPorEmail(emailAutenticado)
                .orElseThrow(() ->
                        new RecursoNaoEncontradoException(
                                "Usuário não encontrado."
                        )
                );

        if (!senhaEncoderGateway.corresponde(
                senhaAtual,
                usuario.getSenha()
        )) {
            throw new RegraNegocioException(
                    "A senha atual está incorreta."
            );
        }

        String senhaHash = senhaEncoderGateway.gerarHash(novaSenha);

        usuarioGateway.atualizarSenha(usuario.getId(), senhaHash);

        recuperacaoSenhaGateway.invalidarPendentes(
                usuario.getId(),
                agora()
        );

        return agora();
    }

    private void validarNovaSenha(
            String novaSenha,
            String confirmacaoSenha
    ) {
        if (novaSenha == null
                || !PADRAO_SENHA.matcher(novaSenha).matches()) {
            throw new RegraNegocioException(
                    "A senha deve ter entre 8 e 100 caracteres, "
                            + "incluindo uma letra maiúscula, "
                            + "uma letra minúscula, um número "
                            + "e um caractere especial, sem espaços."
            );
        }

        if (!novaSenha.equals(confirmacaoSenha)) {
            throw new RegraNegocioException(
                    "A nova senha e a confirmação devem ser iguais."
            );
        }
    }

    private String gerarToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String gerarHashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 não está disponível.",
                    exception
            );
        }
    }

    private RegraNegocioException tokenInvalido() {
        return new RegraNegocioException(
                "Token inválido, expirado ou já utilizado."
        );
    }

    private OffsetDateTime agora() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }
}