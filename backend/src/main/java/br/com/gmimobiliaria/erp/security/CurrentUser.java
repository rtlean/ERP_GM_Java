package br.com.gmimobiliaria.erp.security;

import java.util.Set;

/** Usuário autenticado na requisição, já resolvido para a imobiliária ativa. */
public record CurrentUser(long id, String name, String email, Role role, long companyId, boolean mustChangePassword) {

    public Set<Permission> permissions() {
        return AccessPolicy.permissionsOf(role);
    }
}
