# Contribuindo com o ds-esqueceusenha-api

Ao participar, você concorda em seguir o [Código de Conduta](CODE_OF_CONDUCT.md). Para reportar um problema de segurança, **não** abra uma issue — siga o [SECURITY.md](SECURITY.md).

## Idioma

Documentação, issues, Pull Requests e mensagens ao usuário em português do Brasil.

## Configurando o ambiente

1. Instale o JDK 19.
2. Clone o repositório e entre na pasta:
   ```bash
   git clone https://github.com/EcoCiente-Instituto-J-F/ds-esqueceusenha-api.git
   cd ds-esqueceusenha-api
   ```
3. Copie `.env.example` para `.env` (ignorado pelo Git) e ajuste os valores.
4. Execute com `./gradlew bootRun` (Windows: `.\gradlew.bat bootRun`).

## Testando sua mudança

O CI roda o mesmo comando que você deve rodar antes de abrir a PR:

```bash
./gradlew clean build --no-daemon
```

Toda mudança de comportamento deve vir com teste (JUnit) que falharia sem ela, em `src/test/java`. Os testes não devem depender de rede, APIs pagas ou dados de produção.

## Regras do projeto

- Nenhum segredo (`JWT_SECRET`, credenciais do Firebase, senhas de banco ou SMTP) no repositório; use `.env` (ignorado pelo Git).
- Autorização e regras de senha ficam em `SecurityConfig` e `SenhaService`; não duplique essas regras no controller.
- Mantenha as camadas `core` / `dataprovaider` / `entrypoint`: o `core` depende apenas de gateways (interfaces).
- Novas variáveis lidas em `application.properties` devem ser documentadas no README e no `.env.example`.
- Códigos de recuperação e senhas nunca são gravados nem logados em texto puro.
- Nenhum dado pessoal real em testes ou exemplos.
- Nenhuma dependência nova sem discussão prévia.
- A versão em `build.gradle` (formato `MAJOR.MINOR.PATCH`) define a tag da imagem Docker, que é imutável: aumente a versão quando a mudança precisar ser publicada.

## Pull requests

1. Crie uma branch a partir da `main` (ex.: `feat/nome-da-mudanca`).
2. Faça commits focados com prefixos `feat:`, `fix:`, `chore:`, `docs:`, `test:` ou `refactor:`.
3. Abra a PR para a `main`. O bot do projeto preenche a descrição automaticamente (PRs de forks não recebem esse recurso).
4. O CI precisa passar e um mantenedor revisa antes do merge.

## Reportando bugs e propondo funcionalidades

Abra uma issue com o comportamento esperado, o observado, os passos para reproduzir e a versão. Use dados fictícios. Para vulnerabilidades, siga o [SECURITY.md](SECURITY.md).
