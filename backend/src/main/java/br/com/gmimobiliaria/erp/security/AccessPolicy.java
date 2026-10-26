package br.com.gmimobiliaria.erp.security;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class AccessPolicy {
    private static final Map<Role, Set<Permission>> PERMISSIONS = buildPermissions();

    private AccessPolicy() {
    }

    public static boolean allows(Role role, Permission permission) {
        return role != null && permission != null
                && PERMISSIONS.getOrDefault(role, Set.of()).contains(permission);
    }

    public static Set<Permission> permissionsOf(Role role) {
        return role == null ? Set.of() : Set.copyOf(PERMISSIONS.getOrDefault(role, Set.of()));
    }

    private static Map<Role, Set<Permission>> buildPermissions() {
        var permissions = new EnumMap<Role, Set<Permission>>(Role.class);

        permissions.put(Role.ADMINISTRADOR, EnumSet.allOf(Permission.class));
        permissions.put(Role.GESTOR, EnumSet.of(
                Permission.PROPERTIES_READ,
                Permission.PEOPLE_READ,
                Permission.CRM_READ,
                Permission.CRM_WRITE,
                Permission.CONTRACTS_READ,
                Permission.REPORTS_READ,
                Permission.AUDIT_READ
        ));
        // Espelha o papel "user" do sistema atual (secretária): cadastros, contratos e lançamentos,
        // sem usuários, configurações, exportação e auditoria.
        permissions.put(Role.OPERACIONAL, EnumSet.of(
                Permission.PROPERTIES_READ,
                Permission.PROPERTIES_WRITE,
                Permission.PEOPLE_READ,
                Permission.PEOPLE_WRITE,
                Permission.CONTRACTS_READ,
                Permission.CONTRACTS_WRITE,
                Permission.FINANCE_READ,
                Permission.FINANCE_WRITE,
                Permission.REPORTS_READ
        ));
        permissions.put(Role.CORRETOR, EnumSet.of(
                Permission.PROPERTIES_READ,
                Permission.PROPERTIES_WRITE,
                Permission.PEOPLE_READ,
                Permission.PEOPLE_WRITE,
                Permission.CRM_READ,
                Permission.CRM_WRITE,
                Permission.CONTRACTS_READ
        ));
        permissions.put(Role.LOCACAO, EnumSet.of(
                Permission.PROPERTIES_READ,
                Permission.PEOPLE_READ,
                Permission.CONTRACTS_READ,
                Permission.CONTRACTS_WRITE,
                Permission.DOCUMENTS_READ,
                Permission.DOCUMENTS_WRITE,
                Permission.FINANCE_READ
        ));
        permissions.put(Role.FINANCEIRO, EnumSet.of(
                Permission.PROPERTIES_READ,
                Permission.PEOPLE_READ,
                Permission.CONTRACTS_READ,
                Permission.FINANCE_READ,
                Permission.FINANCE_WRITE,
                Permission.REPORTS_READ
        ));
        permissions.put(Role.ATENDIMENTO, EnumSet.of(
                Permission.PROPERTIES_READ,
                Permission.PEOPLE_READ,
                Permission.PEOPLE_WRITE,
                Permission.CRM_READ,
                Permission.CRM_WRITE
        ));

        return Map.copyOf(permissions);
    }
}
