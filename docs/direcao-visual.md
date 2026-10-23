# Direção visual do ERP

## Identidade

O sistema usará o logotipo oficial da GM Imobiliária, preservado em `frontend/public/brand-logo.svg`.

A interface será criada para equipes imobiliárias que precisam enxergar rapidamente imóveis, contratos, pendências e valores. O layout deve priorizar decisão operacional, não apenas aparência.

## Direção visual

- Azul institucional profundo: `#0A1F3F`
- Azul de marca: `#081F8F`
- Fundo claro azulado: `#F4F7FB`
- Superfície: `#FFFFFF`
- Texto principal: `#10233F`
- Cor da Aba: `#D97706`
- Sinal de atenção: `#D97706`
- Sucesso: `#138A72`
- Erro: `#B42318`

## Layout

O painel principal terá:

- navegação lateral recolhível;
- barra superior com busca global, notificações e contexto da imobiliária;
- cartões de indicadores com leitura rápida;
- área de trabalho com blocos reorganizáveis;
- tabelas com filtros, paginação e ações claras;
- painel lateral para detalhes sem perder o contexto da lista;
- responsividade para computador e tablet.

## Interação

A experiência será dinâmica por meio de:

- filtros persistidos por tela;
- atualização de indicadores após ações concluídas;
- drawers para consulta e edição contextual;
- feedback visual de carregamento, sucesso, erro e estado vazio;
- atalhos de teclado nas áreas de uso frequente;
- transições curtas e respeitando `prefers-reduced-motion`.

## Regra de consistência

Nenhuma interação será apenas visual. Toda ação deverá confirmar:

1. validação de entrada;
2. autorização no backend;
3. persistência;
4. atualização da interface;
5. mensagem clara do resultado.

## Wireframe conceitual

```
┌──────────────┬───────────────────────────────────────────────┐
│ Logo         │ Busca global       Alertas       Usuário       │
│ Navegação    ├───────────────────────────────────────────────┤
│              │ Título da área + ação principal                │
│ Dashboard    │ ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐   │
│ Imóveis      │ │ KPI    │ │ KPI    │ │ KPI    │ │ KPI    │   │
│ Pessoas      │ └────────┘ └────────┘ └────────┘ └────────┘   │
│ CRM          │                                               │
│ Contratos    │ Lista ou painel operacional                    │
│ Financeiro   │ com filtros, estados e ações contextuais       │
│ Documentos   │                                      ┌───────┐ │
│ Configurações│                                      │Detalhe│ │
└──────────────┴──────────────────────────────────────┴───────┘
```

## Critérios de qualidade visual

- foco de teclado visível;
- contraste adequado;
- rótulos acessíveis;
- textos em português do Brasil;
- sem excesso de animações;
- layout funcional em tablet;
- estados de erro e vazio orientando o próximo passo;
- logotipo nunca distorcido ou usado como decoração sem função.
