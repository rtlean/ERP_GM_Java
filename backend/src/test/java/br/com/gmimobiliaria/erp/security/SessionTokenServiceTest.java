package br.com.gmimobiliaria.erp.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class SessionTokenServiceTest {
    private static final String SECRET = "segredo-de-teste-com-mais-de-32-caracteres";
    private static final Instant NOW = Instant.parse("2026-10-26T12:00:00Z");

    private static SessionTokenService service(String secret, Instant now) {
        return new SessionTokenService(new SessionProperties(secret, "erp_gm_session", Duration.ofHours(8), true),
                Clock.fixed(now, ZoneOffset.UTC));
    }

    @Test
    void tokenValidoDevolveUsuarioEEmissao() {
        var token = service(SECRET, NOW).issue(42);
        var claims = service(SECRET, NOW.plusSeconds(60)).verify(token).orElseThrow();
        assertEquals(42, claims.userId());
        assertEquals(NOW, claims.issuedAt());
    }

    @Test
    void tokenExpiradoERecusado() {
        var token = service(SECRET, NOW).issue(42);
        assertTrue(service(SECRET, NOW.plus(Duration.ofHours(8))).verify(token).isEmpty());
    }

    @Test
    void tokenDeOutroSegredoOuAlteradoERecusado() {
        var token = service(SECRET, NOW).issue(42);
        assertTrue(service("outro-segredo-com-mais-de-32-caracteres!!", NOW).verify(token).isEmpty());
        assertTrue(service(SECRET, NOW).verify(token.substring(0, token.length() - 2) + "xx").isEmpty());
        assertTrue(service(SECRET, NOW).verify("lixo").isEmpty());
        assertTrue(service(SECRET, NOW).verify(null).isEmpty());
    }

    @Test
    void segredoCurtoImpedeASubida() {
        assertThrows(IllegalStateException.class, () -> service("curto", NOW));
        assertThrows(IllegalStateException.class, () -> service(null, NOW));
    }
}
