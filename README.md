# ERP GM Imobiliária

Nova implementação do ERP da GM Imobiliária com:

- Frontend: Angular + TypeScript
- Backend: Java + Spring Boot
- Banco de dados: preservado do sistema atual, com migração controlada
- Integrações e documentos: evolução incremental

## Repositórios

- Sistema atual: https://github.com/rtlean/app-web-imobili-ria
- Nova implementação: https://github.com/rtlean/ERP_GM_Java

## Princípios

1. O sistema atual permanece preservado e operacional.
2. Nenhuma alteração de banco será feita sem migração revisada e validada.
3. Autorização será aplicada no backend, não apenas na interface.
4. Dados reais não serão copiados para desenvolvimento sem autorização.
5. Cada módulo será implementado com testes unitários, integração e ponta a ponta quando aplicável.

## Estrutura

- `backend/`: aplicação Spring Boot
- `frontend/`: aplicação Angular
- `database/`: documentação e migrações controladas
- `docs/`: arquitetura, contratos e plano de migração

Consulte [docs/plano-migracao.md](docs/plano-migracao.md) antes de implementar módulos.

## Situação atual

| Módulo | Novo sistema |
|---|---|
| Login (mesmos usuários e senhas) | Pronto |
| Locadores, Imóveis, Locatários | Consulta |
| Contratos, Financeiro, Relatório, Configurações | No sistema atual |

## Rodar localmente

Requisitos: Java 21 e Node 22.22.3+, 24.15.0+ ou 26+ (versões compatíveis com Angular 22).

Com dados fictícios, sem acessar o Supabase:

```bash
cd backend && ./mvnw spring-boot:test-run -Dspring-boot.run.mainClass=br.com.gmimobiliaria.erp.LocalErpApplication
# API em http://localhost:8080
cd frontend && npm install && npm start        # http://localhost:4200
```

Login: `admin@ficticio.local` ou `operacional@ficticio.local`, senha `Senha@123`.

Contra o banco real (o mesmo do sistema atual):

```bash
cd backend
DATABASE_URL='postgresql://...' SESSION_SECRET='...' SESSION_COOKIE_SECURE=false ./mvnw spring-boot:run
```

## Testes

```bash
cd backend && ./mvnw test        # H2 em memória; nunca conecta no Supabase
cd frontend && npm test
```

## Deploy

Veja [docs/deploy-vercel.md](docs/deploy-vercel.md).

Evidências e pendências da última verificação: [validação da implantação](docs/validacao-implantacao.md).
