# Arquitetura alvo inicial

## Visão

```
Angular
   |
   v
Spring Boot REST API
   |
   +-- PostgreSQL existente
   +-- Storage de documentos
   +-- Autenticação e sessões
   +-- Auditoria
   +-- Adaptadores de integração
```

## Backend

O backend será organizado por domínio, com separação entre:

- controllers: HTTP e contratos;
- services: regras de negócio;
- repositories: persistência;
- security: autenticação e autorização;
- audit: eventos críticos;
- integrations: adaptadores externos.

A interface nunca acessará o banco diretamente.

## Frontend

O frontend será organizado por áreas funcionais:

- autenticação;
- imóveis;
- pessoas;
- CRM;
- contratos;
- financeiro;
- documentos;
- indicadores;
- configurações.

Cada tela deverá tratar carregamento, erro, vazio, sucesso, validação e permissões.

## Banco e compatibilidade

O banco atual será tratado como fonte de referência. Antes de qualquer alteração:

- inventariar tabelas, colunas, chaves e índices;
- mapear entidades para o domínio Java;
- confirmar tipos monetários e datas;
- documentar regras de isolamento por empresa;
- validar migrações em ambiente seguro;
- manter rollback possível.

## Segurança

- autorização no servidor;
- menor privilégio;
- isolamento por empresa e filial quando aplicável;
- nenhum segredo no frontend;
- logs sem tokens, senhas ou dados pessoais desnecessários;
- auditoria para operações críticas;
- documentos protegidos e com acesso temporário quando aplicável.