package br.com.gmimobiliaria.erp.auth;

import br.com.gmimobiliaria.erp.audit.AuditService;
import br.com.gmimobiliaria.erp.common.ApiException;
import br.com.gmimobiliaria.erp.security.CurrentUser;
import br.com.gmimobiliaria.erp.security.LegacyPasswordHasher;
import br.com.gmimobiliaria.erp.security.SessionTokenService;
import java.time.Clock;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/** Login com as mesmas credenciais, bloqueios e mensagens do sistema atual. */
@Service
public class AuthService {
    static final String BLOCKED_MESSAGE = "Muitas tentativas de login. Aguarde 15 minutos e tente novamente.";
    static final String INVALID_MESSAGE = "Credenciais inválidas ou expiradas.";
    static final String NO_COMPANY_MESSAGE = "Usuário sem acesso ativo a uma imobiliária.";

    private final UserAccountRepository users;
    private final SessionUserService sessionUsers;
    private final LoginThrottle throttle;
    private final LegacyPasswordHasher passwords;
    private final SessionTokenService tokens;
    private final AuditService audit;
    private final Clock clock;

    public AuthService(UserAccountRepository users, SessionUserService sessionUsers, LoginThrottle throttle,
                       LegacyPasswordHasher passwords, SessionTokenService tokens, AuditService audit, Clock clock) {
        this.users = users;
        this.sessionUsers = sessionUsers;
        this.throttle = throttle;
        this.passwords = passwords;
        this.tokens = tokens;
        this.audit = audit;
        this.clock = clock;
    }

    public LoginResult login(String rawEmail, String password, String ip) {
        var email = rawEmail.trim().toLowerCase(Locale.ROOT);

        // Confere o bloqueio antes do scrypt, que é caro de propósito.
        if (throttle.isBlocked(email, ip)) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_ATTEMPTS", BLOCKED_MESSAGE);
        }

        var account = users.findByEmail(email)
                .filter(user -> user.canHoldSession(clock.instant(), null))
                .filter(user -> passwords.matches(password, user.passwordHash()));
        if (account.isEmpty()) {
            throttle.recordFailure(email, ip);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", INVALID_MESSAGE);
        }

        var user = sessionUsers.withActiveCompany(account.get())
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "NO_ACTIVE_COMPANY", NO_COMPANY_MESSAGE));

        throttle.clear(email, ip);
        users.recordSignIn(user.id(), user.companyId(), clock.instant());
        audit.record(user.id(), user.companyId(), "auth.login", "user", user.id());
        return new LoginResult(tokens.issue(user.id()), user);
    }

    public void logout(CurrentUser user) {
        if (user != null) {
            audit.record(user.id(), user.companyId(), "auth.logout", "user", user.id());
        }
    }

    public record LoginResult(String token, CurrentUser user) {
    }
}
