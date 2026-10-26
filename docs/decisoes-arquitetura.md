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

## Banco compartilhado com o sistema atual (26/10/2026)

O novo backend usa o mesmo Postgres do Supabase (`azdqwlvcltbooxbnjetv`) que o sistema atual, pela mesma `DATABASE_URL`.

- Nenhuma alteração de schema: sem DDL, sem Flyway, sem `ddl-auto`. O backend só mapeia as tabelas existentes.
- O desenvolvimento conecta no banco de produção com leitura e escrita, por decisão do responsável. Por isso, cada caminho de escrita precisa de revisão e testes antes de chegar ao `main`.
- Para trabalhar sem o banco real existe o modo local com dados fictícios (`LocalErpApplication`, ver README).
- Escritas desta fase, as mesmas que o sistema atual faz no login: `login_attempts`, `audit_events` (com `metadata.origin = "erp-gm-java"`) e `users.last_signed_in` / `users.active_company_id`.

## Convivência por módulo

O sistema atual continua sendo o oficial. Cada módulo entra no novo sistema primeiro como consulta, é comparado com o atual e só depois ganha edição. Os módulos ainda não migrados aparecem no menu como "Em migração", com link para o sistema atual.

## Login com as mesmas credenciais

O backend valida o hash scrypt de `users.password_hash` (`salt:hash`, N=16384, r=8, p=1, 64 bytes), o mesmo formato do sistema atual. A sessão do novo sistema é independente: cookie e segredo próprios.

## Hospedagem

Um projeto Vercel com dois services (`vercel.json`): o Angular, servido como arquivos estáticos, e o Spring Boot em container (`backend/Dockerfile.vercel`). Tudo responde no mesmo domínio, e `/api/*` vai para o backend. Por isso não é preciso CORS e o cookie é first-party.
