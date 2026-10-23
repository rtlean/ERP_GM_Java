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

## Listagens

Listagens deverão prever:

- paginação;
- filtros explícitos;
- ordenação permitida por campo;
- limite máximo de itens;
- resposta com total ou cursor documentado.

## Autorização

Respostas de autorização não devem revelar se um registro protegido existe. A operação deve retornar erro seguro e ser auditada quando aplicável.

## Compatibilidade

O contrato da nova API não será considerado estável apenas porque a rota responde. Será validado com testes de entrada válida, entrada inválida, permissão positiva, permissão negativa e persistência.
