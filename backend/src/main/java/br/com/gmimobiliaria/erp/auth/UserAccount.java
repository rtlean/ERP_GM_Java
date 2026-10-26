package br.com.gmimobiliaria.erp.auth;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/** Linha de {@code users} com o que a autenticação precisa. O hash nunca sai do backend. */
public record UserAccount(
        long id,
        String name,
        String email,
        String role,
        String status,
        String passwordHash,
        Instant temporaryAccessExpiresAt,
        boolean mustChangePassword,
        Instant sessionsInvalidatedAt,
        Long activeCompanyId) {

    /** Mesmas regras de {@code validateActiveUserSession} do sistema atual. */
    public boolean canHoldSession(Instant now, Instant sessionIssuedAt) {
        if (!"active".equals(status)) {
            return false;
        }
        if (temporaryAccessExpiresAt != null && !now.isBefore(temporaryAccessExpiresAt)) {
            return false;
        }
        return sessionsInvalidatedAt == null || sessionIssuedAt == null
                || !sessionIssuedAt.isBefore(sessionsInvalidatedAt.truncatedTo(ChronoUnit.SECONDS));
    }
}
