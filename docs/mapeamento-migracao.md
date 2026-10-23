# Inventário inicial de migração

## Sistema atual

Repositório de referência: https://github.com/rtlean/app-web-imobili-ria

## Áreas identificadas

- autenticação e sessão;
- usuários e permissões;
- imóveis;
- proprietários;
- locatários;
- contratos;
- lançamentos financeiros;
- despesas;
- pendências;
- repasses;
- relatórios PDF e XLSX;
- documentos;
- configurações de marca;
- auditoria.

## Ordem de migração

1. saúde da aplicação e configuração;
2. autenticação e usuários;
3. permissões e auditoria;
4. imóveis e pessoas;
5. contratos;
6. lançamentos e pendências;
7. repasses e relatórios;
8. documentos e integrações.

## Para cada área

Registrar antes da implementação:

- telas existentes;
- rotas e operações;
- tabelas utilizadas;
- validações;
- regras de negócio;
- permissões;
- mensagens de erro;
- testes existentes;
- comportamento que precisa permanecer compatível.

## Regra de segurança

Dados reais não serão copiados para desenvolvimento. A validação inicial usará fixtures controladas e dados fictícios claramente identificados.
