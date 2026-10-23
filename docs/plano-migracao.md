# Plano inicial de migração

## Objetivo

Reimplementar o ERP atual em Angular e Spring Boot preservando comportamento, dados, permissões e rastreabilidade.

## Sistema de referência

O sistema atual é o repositório `rtlean/app-web-imobili-ria`. Ele permanece como referência funcional durante a migração.

## Estratégia

A migração será incremental:

1. mapear módulos, rotas, regras, tabelas e permissões;
2. definir contratos de API;
3. criar a fundação do backend e frontend;
4. implementar autenticação e contexto de empresa;
5. implementar imóveis e pessoas;
6. validar cada módulo contra o sistema atual;
7. migrar gradualmente CRM, contratos, financeiro e relatórios;
8. executar piloto em homologação;
9. definir a troca de operação somente após evidências.

## Restrições

- não alterar o banco atual sem decisão registrada;
- não executar migração destrutiva;
- não copiar dados reais para desenvolvimento sem autorização;
- não considerar uma tela pronta sem testar persistência e autorização;
- não considerar o novo sistema equivalente apenas por aparência visual.

## Critérios por módulo

- regra de negócio documentada;
- contrato de entrada e saída definido;
- autorização positiva e negativa testada;
- testes unitários e de integração;
- estados de carregamento, erro, vazio e sucesso;
- auditoria de ações críticas;
- validação de dados financeiros sem ponto flutuante;
- plano de rollback.