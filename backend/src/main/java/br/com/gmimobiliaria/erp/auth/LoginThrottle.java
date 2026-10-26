package br.com.gmimobiliaria.erp.auth;

import java.time.Clock;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

/**
 * Limite de tentativas de login na tabela {@code login_attempts}, compartilhada com o sistema atual: errar a senha
 * em um sistema conta no outro. Mesma janela (15 min), teto (10) e escopos ("email" e "ip").
 */
@Component
public class LoginThrottle {
    static final Duration WINDOW = Duration.ofMinutes(15);
    static final int MAX_ATTEMPTS = 10;
    private static final Duration RETENTION = Duration.ofHours(24);
    private static final Logger log = LoggerFactory.getLogger(LoginThrottle.class);

    private final JdbcClient jdbc;
    private final Clock clock;

    public LoginThrottle(JdbcClient jdbc, Clock clock) {
        this.jdbc = jdbc;
        this.clock = clock;
    }

    public boolean isBlocked(String email, String ip) {
        var since = now().minus(WINDOW);
        return countSince("email", email, since) >= MAX_ATTEMPTS || countSince("ip", ip, since) >= MAX_ATTEMPTS;
    }

    /** Registra a falha nos dois escopos. Falha própria nunca interrompe o login. */
    public void recordFailure(String email, String ip) {
        try {
            insert("email", email);
            insert("ip", ip);
            jdbc.sql("DELETE FROM login_attempts WHERE attempted_at < :limit")
                    .param("limit", now().minus(RETENTION))
                    .update();
        } catch (RuntimeException error) {
            log.error("Falha ao registrar tentativa de login", error);
        }
    }

    /** Login certo zera e-mail e IP: o escritório inteiro sai pelo mesmo IP. */
    public void clear(String email, String ip) {
        try {
            delete("email", email);
            delete("ip", ip);
        } catch (RuntimeException error) {
            log.error("Falha ao limpar tentativas de login", error);
        }
    }

    private int countSince(String scope, String subject, OffsetDateTime since) {
        if (subject == null || subject.isBlank()) {
            return 0;
        }
        return jdbc.sql("""
                        SELECT count(*) FROM login_attempts
                         WHERE scope = :scope AND subject = :subject AND attempted_at >= :since
                        """)
                .param("scope", scope)
                .param("subject", subject)
                .param("since", since)
                .query(Integer.class)
                .single();
    }

    private void insert(String scope, String subject) {
        if (subject == null || subject.isBlank()) {
            return;
        }
        jdbc.sql("INSERT INTO login_attempts (scope, subject, attempted_at) VALUES (:scope, :subject, :now)")
                .param("scope", scope)
                .param("subject", subject)
                .param("now", now())
                .update();
    }

    private void delete(String scope, String subject) {
        if (subject == null || subject.isBlank()) {
            return;
        }
        jdbc.sql("DELETE FROM login_attempts WHERE scope = :scope AND subject = :subject")
                .param("scope", scope)
                .param("subject", subject)
                .update();
    }

    private OffsetDateTime now() {
        return OffsetDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
    }
}
