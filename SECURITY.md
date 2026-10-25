# Política de Segurança

O ds-esqueceusenha-api trata e-mails de usuários, hashes de senha, códigos de recuperação de senha e as credenciais de acesso ao Firebase e ao servidor de e-mail.

## Versões suportadas

| Versão | Suporte |
| --- | --- |
| `main` (mais recente) | Sim |
| Demais branches | Não |

## Reportando uma vulnerabilidade

**Não abra issue, Pull Request ou discussão pública** para relatar vulnerabilidades.

Use o relato privado do GitHub: <https://github.com/EcoCiente-Instituto-J-F/ds-esqueceusenha-api/security/advisories/new> (Security → Advisories → Report a vulnerability).

> Mantenedores: ative *Private vulnerability reporting* em Settings → Code security.

Inclua no relato:

- o impacto da falha;
- os passos para reproduzir, usando **dados fictícios**;
- o commit testado e a configuração;
- se possível, uma ideia de correção.

Depois do relato: confirmaremos o recebimento, corrigiremos de forma privada e publicaremos um advisory com crédito a quem reportou, se desejado. Pedimos um prazo razoável para a correção antes de qualquer divulgação pública.

## Escopo

**Dentro do escopo**

- Burla de autenticação (JWT) ou de autorização, em especial em `/senhas/alterar`
- Redefinir a senha de outro usuário sem o código válido
- Enumeração de contas pelas respostas de `/senhas/esqueceu` e `/senhas/redefinir`
- Força bruta do código de 4 dígitos, ausência de limite de tentativas ou de requisições
- Vazamento de senhas, códigos, hashes, `JWT_SECRET` ou credenciais do Firebase em logs, respostas ou repositório
- Injeção SQL/JPQL ou de cabeçalhos de e-mail
- Segredos commitados e falhas nos workflows (`pr-bot.yml`, `ci.yml`) que exponham segredos

**Fora do escopo**

- Bugs sem impacto de segurança
- Falhas em serviços de terceiros (Firebase, provedor SMTP, GitHub, Docker Hub)
- Negação de serviço no próprio deploy

## Como o sistema protege os dados

| Camada | Onde | O que faz |
| --- | --- | --- |
| Autenticação | `config/security/JwtService.java`, `JwtAuthenticationFilter.java` | Valida a assinatura (HMAC) e a expiração do JWT; sessão stateless |
| Autorização | `config/security/SecurityConfig.java` | Só `/senhas/esqueceu`, `/senhas/redefinir`, Swagger e `/actuator/health` são públicos; o restante exige autenticação |
| Senhas | `SenhaEncoderGatewayImpl`, `SenhaService` | BCrypt; política de senha forte; confirmação obrigatória |
| Código de recuperação | `SenhaService`, `tb_esqueci_senha` | 4 dígitos gerados com `SecureRandom`, guardados só como hash, validade de 15 minutos, uso único, códigos pendentes invalidados |
| Enumeração de contas | `SenhaService.solicitarRecuperacao` | Responde da mesma forma exista ou não o e-mail |
| Erros | `entrypoint/exception/ApiExceptionHandler.java` | Respostas de erro padronizadas |
| Transporte de e-mail | `application.properties` | STARTTLS obrigatório e timeouts de 5 s |

Observação: não há limite de tentativas ou de requisições nas rotas públicas; trate isso na borda (gateway/ingress) ou no código.

## Checklist de deploy

- `JWT_SECRET` longo e aleatório, `FIREBASE_CREDENCIAIS`, `DB_PASSWORD` e `MAIL_PASSWORD` fora de imagem, repositório e ConfigMap (use Secrets).
- `DDL_AUTO=validate` e `SHOW_SQL=false` em produção.
- Usuário de banco com o mínimo de permissões necessárias.
- Aplicar limite de requisições em `/senhas/esqueceu` e `/senhas/redefinir`.
- Avaliar restringir `/swagger-ui.html` e `/v3/api-docs` em produção.
- Servir somente via HTTPS.
