package br.com.ecociente.senha.dataprovaider.gateway;

import br.com.ecociente.senha.core.exception.SincronizacaoSenhaException;
import br.com.ecociente.senha.core.gateway.FirebaseSenhaGateway;
import com.google.firebase.auth.AuthErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class FirebaseSenhaGatewayImpl implements FirebaseSenhaGateway {

    private final FirebaseAuth firebaseAuth;

    @Override
    public void atualizarSenha(String email, String novaSenha) {
        try {
            UserRecord usuario = firebaseAuth.getUserByEmail(
                    email.trim().toLowerCase(Locale.ROOT)
            );

            firebaseAuth.updateUser(
                    new UserRecord.UpdateRequest(usuario.getUid())
                            .setPassword(novaSenha)
            );
        } catch (FirebaseAuthException exception) {
            if (exception.getAuthErrorCode() == AuthErrorCode.USER_NOT_FOUND) {
                return;
            }

            throw new SincronizacaoSenhaException(
                    "Não foi possível atualizar a senha no Firebase.",
                    exception
            );
        }
    }
}
