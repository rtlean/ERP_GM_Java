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