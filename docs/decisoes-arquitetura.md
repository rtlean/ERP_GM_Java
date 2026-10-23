# Decisões de arquitetura

## Empresa atendida

O ERP será utilizado exclusivamente pela GM Imobiliária.

### Consequências

- não haverá cadastro de múltiplas imobiliárias no produto;
- não haverá seletor de imobiliária no login;
- não haverá troca de contexto entre empresas;
- tabelas do domínio não precisarão de uma chave de tenant para suportar outra imobiliária;
- a autorização continuará sendo aplicada no backend;
- filiais, equipes e responsabilidades poderão existir dentro da GM Imobiliária;
- uma futura expansão para multiempresa exigirá uma decisão e uma migração planejada.

## Banco atual

O banco atual será preservado como referência e fonte de dados da operação durante a migração.

Nenhuma tabela será removida ou alterada nesta etapa.

## Escopo desta fase

- documentar a decisão de empresa única;
- definir usuários e perfis;
- preparar contratos de API;
- validar a fundação Angular e Spring Boot;
- não implementar ainda regras financeiras ou migrações de dados.
