package br.com.ecociente.senha.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Bean
    FirebaseAuth firebaseAuth(
            @Value("${app.firebase.credenciais}") String credenciais
    ) throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(
                    FirebaseOptions.builder()
                            .setCredentials(
                                    GoogleCredentials.fromStream(
                                            new ByteArrayInputStream(
                                                    credenciais.getBytes(StandardCharsets.UTF_8)
                                            )
                                    )
                            )
                            .build()
            );
        }

        return FirebaseAuth.getInstance();
    }
}
