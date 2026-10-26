package br.com.gmimobiliaria.erp.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AccessPolicyTest {
    @Test
    void administradorPossuiTodasAsPermissoes() {
        assertTrue(AccessPolicy.allows(Role.ADMINISTRADOR, Permission.FINANCE_WRITE));
        assertTrue(AccessPolicy.allows(Role.ADMINISTRADOR, Permission.AUDIT_READ));
    }

    @Test
    void corretorNaoPodeAlterarFinanceiro() {
        assertFalse(AccessPolicy.allows(Role.CORRETOR, Permission.FINANCE_WRITE));
    }

    @Test
    void atendimentoPodeGerenciarCrmMasNaoAuditoria() {
        assertTrue(AccessPolicy.allows(Role.ATENDIMENTO, Permission.CRM_WRITE));
        assertFalse(AccessPolicy.allows(Role.ATENDIMENTO, Permission.AUDIT_READ));
    }

    @Test
    void papeisDoSistemaAtualSaoMapeados() {
        assertTrue(Role.fromLegacy("admin") == Role.ADMINISTRADOR);
        assertTrue(Role.fromLegacy("user") == Role.OPERACIONAL);
        assertTrue(Role.fromLegacy(null) == Role.OPERACIONAL);
    }

    @Test
    void operacionalTemCadastrosMasNaoUsuariosNemAuditoria() {
        assertTrue(AccessPolicy.allows(Role.OPERACIONAL, Permission.PEOPLE_READ));
        assertTrue(AccessPolicy.allows(Role.OPERACIONAL, Permission.PROPERTIES_WRITE));
        assertFalse(AccessPolicy.allows(Role.OPERACIONAL, Permission.USERS_WRITE));
        assertFalse(AccessPolicy.allows(Role.OPERACIONAL, Permission.AUDIT_READ));
    }

    @Test
    void perfilOuPermissaoNulosSaoNegados() {
        assertFalse(AccessPolicy.allows(null, Permission.PROPERTIES_READ));
        assertFalse(AccessPolicy.allows(Role.ADMINISTRADOR, null));
    }
}
