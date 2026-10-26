package br.com.gmimobiliaria.erp.auth;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class UserAccountRepository {
    private static final String SELECT_USER = """
            SELECT id, name, email, role, status, password_hash, temporary_access_expires_at,
                   must_change_password, sessions_invalidated_at, active_company_id
              FROM users
            """;

    private final JdbcClient jdbc;

    public UserAccountRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<UserAccount> findByEmail(String email) {
        return jdbc.sql(SELECT_USER + " WHERE email = :email LIMIT 1")
                .param("email", email)
                .query(UserAccountRepository::map)
                .optional();
    }

    public Optional<UserAccount> findById(long id) {
        return jdbc.sql(SELECT_USER + " WHERE id = :id")
                .param("id", id)
                .query(UserAccountRepository::map)
                .optional();
    }

    public List<Membership> activeMemberships(long userId) {
        return jdbc.sql("""
                        SELECT company_id, role
                          FROM company_users
                         WHERE user_id = :userId AND status = 'active'
                         ORDER BY company_id
                         LIMIT 2
                        """)
                .param("userId", userId)
                .query((rs, row) -> new Membership(rs.getLong("company_id"), rs.getString("role")))
                .list();
    }

    /** Mesmo efeito do login do sistema atual: registra o acesso e fixa a imobiliária ativa. */
    public void recordSignIn(long userId, long companyId, Instant now) {
        jdbc.sql("""
                        UPDATE users
                           SET last_signed_in = :now,
                               active_company_id = :companyId
                         WHERE id = :userId
                        """)
                .param("now", OffsetDateTime.ofInstant(now, ZoneOffset.UTC))
                .param("companyId", companyId)
                .param("userId", userId)
                .update();
    }

    private static UserAccount map(ResultSet rs, int row) throws SQLException {
        return new UserAccount(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("role"),
                rs.getString("status"),
                rs.getString("password_hash"),
                instant(rs, "temporary_access_expires_at"),
                rs.getBoolean("must_change_password"),
                instant(rs, "sessions_invalidated_at"),
                rs.getObject("active_company_id", Long.class));
    }

    private static Instant instant(ResultSet rs, String column) throws SQLException {
        var value = rs.getObject(column, OffsetDateTime.class);
        return value == null ? null : value.toInstant();
    }

    public record Membership(long companyId, String role) {
    }
}
