package br.com.gmimobiliaria.erp.auth;

import br.com.gmimobiliaria.erp.auth.UserAccountRepository.Membership;
import br.com.gmimobiliaria.erp.security.CurrentUser;
import br.com.gmimobiliaria.erp.security.Role;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/** Transforma o id da sessão em usuário autorizado, com as mesmas regras de acesso do sistema atual. */
@Service
public class SessionUserService {
    private final UserAccountRepository users;
    private final Clock clock;

    public SessionUserService(UserAccountRepository users, Clock clock) {
        this.users = users;
        this.clock = clock;
    }

    public Optional<CurrentUser> load(long userId, Instant sessionIssuedAt) {
        return users.findById(userId)
                .filter(user -> user.canHoldSession(clock.instant(), sessionIssuedAt))
                .flatMap(this::withActiveCompany);
    }

    Optional<CurrentUser> withActiveCompany(UserAccount user) {
        var memberships = users.activeMemberships(user.id());
        return resolveActiveMembership(user.activeCompanyId(), memberships)
                .map(membership -> new CurrentUser(
                        user.id(),
                        user.name(),
                        user.email(),
                        Role.fromLegacy(membership.role()),
                        membership.companyId(),
                        user.mustChangePassword()));
    }

    /**
     * Igual a {@code resolveActiveCompanyId} do sistema atual: mantém a imobiliária ativa se o vínculo continuar
     * ativo; senão, só escolhe sozinho quando há exatamente um vínculo.
     */
    static Optional<Membership> resolveActiveMembership(Long currentCompanyId, List<Membership> memberships) {
        if (currentCompanyId != null) {
            var current = memberships.stream().filter(m -> m.companyId() == currentCompanyId).findFirst();
            if (current.isPresent()) {
                return current;
            }
        }
        var distinct = memberships.stream().map(Membership::companyId).distinct().count();
        return distinct == 1 ? Optional.of(memberships.getFirst()) : Optional.empty();
    }
}
