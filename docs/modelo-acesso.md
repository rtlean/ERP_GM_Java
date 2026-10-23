# Modelo inicial de acesso

## Princípios

O ERP será exclusivo da GM Imobiliária, mas nem todo usuário poderá acessar todas as funções.

A interface apenas oculta ou desabilita ações. A validação obrigatória ocorrerá no backend.

## Perfis iniciais

| Perfil | Responsabilidades |
|---|---|
| Administrador | Usuários, permissões, configurações e visão completa |
| Gestor | Acompanhamento operacional, aprovações e indicadores |
| Corretor | Imóveis, pessoas, leads, visitas, propostas e tarefas |
| Locação | Contratos, pendências, cobranças e manutenção |
| Financeiro | Lançamentos, repasses, comissões e relatórios financeiros |
| Atendimento | Cadastro, triagem, agenda e acompanhamento |

## Regras

- usuário inativo não pode iniciar sessão;
- usuário autenticado não pode elevar o próprio perfil;
- operações críticas devem gerar auditoria;
- identificadores recebidos pela API devem ser autorizados no servidor;
- dados sensíveis não devem aparecer em listas sem necessidade;
- ações financeiras devem validar estado, valores e duplicidade;
- documentos devem validar acesso antes do download.

## Testes obrigatórios

- perfil autorizado consegue executar a operação;
- perfil não autorizado recebe resposta negada;
- acesso direto a identificador de outro escopo é rejeitado;
- usuário inativo não acessa a API;
- logout encerra a sessão;
- ações críticas geram evento de auditoria.
