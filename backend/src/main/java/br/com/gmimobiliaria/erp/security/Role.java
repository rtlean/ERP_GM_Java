package br.com.gmimobiliaria.erp.security;

public enum Role {
    ADMINISTRADOR,
    GESTOR,
    CORRETOR,
    LOCACAO,
    FINANCEIRO,
    ATENDIMENTO,
    OPERACIONAL;

    /**
     * Papel gravado pelo sistema atual em {@code company_users.role} / {@code users.role} (enum {@code user_role}).
     * Enquanto o banco for compartilhado, só existem "admin" e "user"; os demais perfis aguardam decisão de migração.
     */
    public static Role fromLegacy(String legacyRole) {
        return "admin".equals(legacyRole) ? ADMINISTRADOR : OPERACIONAL;
    }
}
