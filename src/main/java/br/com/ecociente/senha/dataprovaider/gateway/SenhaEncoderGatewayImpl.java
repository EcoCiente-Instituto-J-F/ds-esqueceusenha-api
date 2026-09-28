package br.com.ecociente.senha.dataprovaider.gateway;

import br.com.ecociente.senha.core.exception.RegraNegocioException;
import br.com.ecociente.senha.core.gateway.SenhaEncoderGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class SenhaEncoderGatewayImpl implements SenhaEncoderGateway {

    private final PasswordEncoder passwordEncoder;

    @Override
    public String gerarHash(String senha) {
        if (senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new RegraNegocioException(
                    "A senha excede o limite de 72 bytes permitido pelo BCrypt."
            );
        }

        return passwordEncoder.encode(senha);
    }

    @Override
    public boolean corresponde(String senha, String senhaHash) {
        if (senha == null || senhaHash == null) {
            return false;
        }

        if (senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            return false;
        }

        return passwordEncoder.matches(senha, senhaHash);
    }
}
