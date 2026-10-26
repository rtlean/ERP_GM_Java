# Deploy na Vercel

Domínio: https://erp-gm-imobiliaria-java-angular.vercel.app/

O sistema atual (https://www.admgmimobiliaria.com.br/) fica em outro projeto Vercel e não é afetado.

## Estrutura

`vercel.json` define dois services no mesmo projeto:

| Service | Pasta | Como roda | Rotas |
|---|---|---|---|
| `backend` | `backend/` | Container (`Dockerfile.vercel`), Java 21, porta 80 | `/api/*` |
| `frontend` | `frontend/` | Build Angular estático (`dist/erp-gm-frontend/browser`) | todo o resto |

Vercel Services está em beta. O arquivo CDS gerado no build busca reduzir a inicialização da JVM; o tempo de partida precisa ser medido no ambiente publicado.

Consulte as [evidências e pendências da implantação](validacao-implantacao.md) antes da liberação.

## Primeira configuração

1. Na Vercel, importe o repositório `rtlean/ERP_GM_Java` como **novo projeto**, com Root Directory na raiz do repositório.
2. Em Settings → Domains, use `erp-gm-imobiliaria-java-angular.vercel.app`.
3. Em Settings → Environment Variables (Production e Preview):

   | Variável | Valor |
   |---|---|
   | `DATABASE_URL` | A mesma do projeto Vercel do sistema atual (pooler do Supabase) |
   | `SESSION_SECRET` | Novo, exclusivo deste projeto: `openssl rand -base64 48` |

4. Faça o deploy (push no `main` ou `vercel deploy --prod`).

### Formato da conexão

O nome deve ser exatamente `DATABASE_URL`. O valor deve ser apenas a URI
`postgresql://USUARIO:SENHA_CODIFICADA@HOST:PORTA/postgres`, sem aspas, sem
`DATABASE_URL=` e sem os colchetes usados como marcadores nos exemplos do provedor.
Use percent-encoding nos caracteres especiais da senha (`#` → `%23`, `@` → `%40`,
`[` → `%5B`, `]` → `%5D`). Preserve caracteres que realmente fazem parte da senha.
A URL HTTPS do projeto e as chaves de API não são conexões PostgreSQL.
Após alterar a variável em Production, faça um novo deploy para aplicar o valor.

## Verificação após cada deploy

1. `GET /api/v1/health` responde `{"status":"ok","service":"erp-backend"}`.
2. `/` abre a tela de login.
3. Entrar com um usuário real e conferir Locadores, Imóveis e Locatários contra o sistema atual (mesmos totais).
4. Sair, e confirmar que `/api/v1/owners` passa a responder 401.

## Segurança

- Nenhum segredo vai para o frontend: o Angular só chama `/api` no mesmo domínio.
- `SESSION_SECRET` não deve ser o `JWT_SECRET` do sistema atual.
- Previews da Vercel usam o mesmo banco de produção. Proteja-os com Deployment Protection (Settings → Deployment Protection).
