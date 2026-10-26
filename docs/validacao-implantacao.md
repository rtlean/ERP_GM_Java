# Validação da implantação — 26/10/2026

## Ajustes desta etapa

- Maven Surefire carrega o agente Mockito ao iniciar a JVM, evitando a falha de self-attach observada no ambiente local.
- Requisito de Node alinhado ao Angular instalado, no package.json, lockfile e README.

## Evidências locais

| Verificação | Resultado |
|---|---|
| Backend: `./mvnw -B clean verify` | Aprovado: 44 testes, zero falhas/erros, JAR gerado |
| Frontend: `npm test` | Aprovado: 8 testes em 2 arquivos |
| Frontend: `npm run build` | Aprovado: bundle de produção gerado |
| `git diff --check` | Aprovado |

Backend executado com o Java 25.0.4 embutido no IntelliJ, compilando para Java 21. Testes usam H2 com dados fictícios. Frontend executado com Node 24.15.0 temporário em `/private/tmp/erp-runtime`; o Node padrão 24.14.0 não atende ao Angular instalado. O build Angular abortou com código 134 no sandbox e passou fora dele.

## Situação remota verificada

- Projeto Vercel: `erp-gm-imobiliaria-java-angular`.
- Último deploy consultado: `dpl_5NMRZQY3rbfD5XHhPvqmPbjfEVZs`, estado `READY`, commit `c988241dc1c1c95c9c4044042dee6eba5e3e0333`.
- Esse commit não inclui as alterações locais atuais; `READY` não comprova o funcionamento desta versão.
- Consulta de variáveis do projeto retornou lista vazia. Faltam `DATABASE_URL` e `SESSION_SECRET`.
- Nenhum novo deploy, alteração no banco ou alteração no sistema atual foi realizado nesta etapa.

## Pendências para liberação

1. Definir o banco de destino e configurar as duas variáveis pelo canal seguro da Vercel.
2. Publicar a versão validada no projeto novo.
3. Validar o container Java 21 e o roteamento no ambiente remoto.
4. Executar smoke test de saúde, login, consultas e logout; validar acesso sem sessão e isolamento de imobiliária.

Não executados: build Docker (Docker indisponível localmente), teste visual/E2E em navegador e testes contra banco real. Recomendação: seguir com configuração e deploy controlado; a entrada em operação depende dessas verificações remotas.
