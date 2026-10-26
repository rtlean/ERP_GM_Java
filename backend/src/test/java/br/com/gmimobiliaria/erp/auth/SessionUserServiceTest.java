package br.com.gmimobiliaria.erp.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.gmimobiliaria.erp.auth.UserAccountRepository.Membership;
import java.util.List;
import org.junit.jupiter.api.Test;

class SessionUserServiceTest {
    @Test
    void mantemImobiliariaAtivaQuandoVinculoContinuaAtivo() {
        var resolved = SessionUserService.resolveActiveMembership(2L,
                List.of(new Membership(1, "user"), new Membership(2, "admin")));
        assertEquals(2, resolved.orElseThrow().companyId());
    }

    @Test
    void escolheSozinhoSoComUmVinculo() {
        assertEquals(1, SessionUserService.resolveActiveMembership(null, List.of(new Membership(1, "user")))
                .orElseThrow().companyId());
        assertEquals(1, SessionUserService.resolveActiveMembership(9L, List.of(new Membership(1, "user")))
                .orElseThrow().companyId());
    }

    @Test
    void semVinculoOuAmbiguoNaoTemImobiliaria() {
        assertTrue(SessionUserService.resolveActiveMembership(null, List.of()).isEmpty());
        assertTrue(SessionUserService.resolveActiveMembership(null,
                List.of(new Membership(1, "user"), new Membership(2, "user"))).isEmpty());
    }
}
