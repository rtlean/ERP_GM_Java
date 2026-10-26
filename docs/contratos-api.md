# Contrato inicial da API

## Prefixo

Todas as rotas da nova API usarão o prefixo:

`/api/v1`

## Saúde

`GET /api/v1/health`

Resposta:

```json
{
  "status": "ok",
  "service": "erp-backend"
}
```

## Erros

A API deverá retornar uma estrutura consistente:

```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Não foi possível salvar o registro.",
    "details": [
      {
        "field": "nome",
        "message": "Informe o nome."
      }
    ],
    "requestId": "..."
  }
}
```

O `requestId` também é devolvido no cabeçalho `X-Request-Id` de toda resposta.

## Listagens

Listagens deverão prever:

- paginação;
- filtros explícitos;
- ordenação permitida por campo;
- limite máximo de itens;
- resposta com total ou cursor documentado.

Formato adotado:

- parâmetros `q` (busca), `page` (começa em 0) e `size` (padrão 25, máximo 100);
- resposta `{ "items": [...], "page": 0, "size": 25, "total": 123 }`;
- curingas digitados na busca (`%`, `_`) são tratados como texto.

## Autorização

Respostas de autorização não devem revelar se um registro protegido existe. A operação deve retornar erro seguro e ser auditada quando aplicável.

## Compatibilidade

O contrato da nova API não será considerado estável apenas porque a rota responde. Será validado com testes de entrada válida, entrada inválida, permissão positiva, permissão negativa e persistência.

## Sessão

A sessão fica em cookie `erp_gm_session` (httpOnly, Secure, SameSite=Lax, 8 horas), assinado com `SESSION_SECRET`. É independente do cookie do sistema atual.

Requisições que alteram estado (`POST`, `PUT`, `PATCH`, `DELETE`) exigem o cabeçalho `X-XSRF-TOKEN` com o valor do cookie `XSRF-TOKEN`. O Angular faz isso automaticamente.

| Método | Rota | Acesso | Descrição |
|---|---|---|---|
| `GET` | `/api/v1/auth/csrf` | público | Emite o cookie `XSRF-TOKEN` (204) |
| `POST` | `/api/v1/auth/login` | público | `{ "email", "password" }` → usuário da sessão + cookie |
| `POST` | `/api/v1/auth/logout` | público | Remove o cookie (204) e audita quando há sessão |
| `GET` | `/api/v1/auth/me` | sessão | Usuário da sessão |

Erros do login (mesmas mensagens do sistema atual):

| Status | `code` | Situação |
|---|---|---|
| 400 | `VALIDATION_ERROR` | E-mail ou senha em formato inválido |
| 401 | `INVALID_CREDENTIALS` | Senha errada, usuário inativo ou acesso temporário expirado |
| 403 | `NO_ACTIVE_COMPANY` | Usuário sem vínculo ativo com a imobiliária |
| 429 | `TOO_MANY_ATTEMPTS` | 10 falhas em 15 minutos por e-mail ou IP (tabela `login_attempts`, compartilhada) |

Usuário da sessão:

```json
{
  "id": 1,
  "name": "Nome",
  "email": "pessoa@exemplo.com",
  "role": "ADMINISTRADOR",
  "permissions": ["PEOPLE_READ", "PROPERTIES_READ"],
  "mustChangePassword": false
}
```

## Cadastros (somente leitura)

Todas as listas são filtradas pela imobiliária ativa do usuário. CPF é mascarado (`***.456.789-**`).

| Método | Rota | Permissão | Filtros |
|---|---|---|---|
| `GET` | `/api/v1/owners` | `PEOPLE_READ` | `q`: nome ou e-mail |
| `GET` | `/api/v1/properties` | `PROPERTIES_READ` | `q`: código, endereço, cidade ou locador; `status`: `occupied`, `vacant`, `maintenance` |
| `GET` | `/api/v1/tenants` | `PEOPLE_READ` | `q`: nome ou e-mail |

Valores monetários chegam em centavos inteiros (`rentCents`).
