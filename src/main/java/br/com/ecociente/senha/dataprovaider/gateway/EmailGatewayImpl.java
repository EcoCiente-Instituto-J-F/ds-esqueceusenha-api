package br.com.ecociente.senha.dataprovaider.gateway;

import br.com.ecociente.senha.core.exception.EnvioEmailException;
import br.com.ecociente.senha.core.gateway.EmailGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class EmailGatewayImpl implements EmailGateway {

    private final JavaMailSender mailSender;
    private final String remetente;
    private final String urlRedefinicao;

    public EmailGatewayImpl(
            JavaMailSender mailSender,
            @Value("${app.mail.remetente}") String remetente,
            @Value("${app.senha.url-redefinicao}") String urlRedefinicao
    ) {
        this.mailSender = mailSender;
        this.remetente = remetente;
        this.urlRedefinicao = urlRedefinicao;
    }

    @Override
    public void enviarRecuperacao(String destinatario, String token) {
        String link = UriComponentsBuilder
                .fromUriString(urlRedefinicao)
                .queryParam("token", token)
                .build()
                .encode()
                .toUriString();

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject("Ecociente - Recuperação de senha");
        mensagem.setText("""
                Recebemos uma solicitação para redefinir sua senha.

                Acesse o link abaixo:
                %s

                O link tem validade de 15 minutos e pode ser utilizado uma única vez.

                Se você não solicitou esta alteração, ignore este e-mail.
                """.formatted(link));

        try {
            mailSender.send(mensagem);
        } catch (MailException exception) {
            throw new EnvioEmailException(
                    "Não foi possível enviar o e-mail de recuperação.",
                    exception
            );
        }
    }
}