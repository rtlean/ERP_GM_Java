package br.com.gmimobiliaria.erp;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {
    /** Hash gerado pelo Node do sistema atual para a senha "Senha@123". */
    private static final String HASH = "0123456789abcdef0123456789abcdef:"
            + "72fb9f406c870be54290efe23dfdab096d60a4181d2440ee3e0bbd80e8e72178"
            + "b2dde38c6484a23be70f3bb3b092c3d5592a9fdfd7c5437f570ca5c21afe866b";
    private static final String PASSWORD = "Senha@123";
    private static final String COOKIE = "erp_gm_session";

    @Autowired
    MockMvc mvc;
    @Autowired
    JdbcClient jdbc;

    @BeforeEach
    void dadosFicticios() {
        for (var table : new String[] {"audit_events", "login_attempts", "contracts", "tenants", "properties",
                "owners", "company_users", "users", "companies"}) {
            jdbc.sql("DELETE FROM " + table).update();
        }
        jdbc.sql("INSERT INTO companies (id, name, owner_user_id) VALUES (1, 'GM Teste', 1), (2, 'Outra', 3)").update();
        user(1, "admin@teste.local", "active", null);
        user(2, "operacional@teste.local", "active", null);
        user(3, "outra@teste.local", "active", null);
        user(4, "inativo@teste.local", "inactive", null);
        user(5, "semvinculo@teste.local", "active", null);
        membership(1, 1, "admin");
        membership(1, 2, "user");
        membership(2, 3, "admin");
        membership(1, 4, "user");

        jdbc.sql("""
                INSERT INTO owners (id, full_name, document, email, company_id) VALUES
                  (10, 'Ana Locadora', '123.456.789-01', 'ana@teste.local', 1),
                  (11, 'Bruno Locador', '12345678000190', null, 1),
                  (12, 'Locador de Outra Imobiliária', null, null, 2)
                """).update();
        jdbc.sql("""
                INSERT INTO properties (id, code, type, address, city, state, rent_cents, occupancy_status, owner_id, company_id) VALUES
                  (20, 'IM-001', 'Casa', 'Rua A, 1', 'Uberlândia', 'MG', 150000, 'occupied', 10, 1),
                  (21, 'IM-002', 'Apartamento', 'Rua B, 2', 'Uberlândia', 'MG', 98050, 'vacant', 11, 1),
                  (22, 'IM-003', 'Sala', 'Rua C, 3', 'Uberaba', 'MG', 50000, 'occupied', 11, 1),
                  (23, 'X-001', 'Casa', 'Rua Z, 9', 'Outra', 'SP', 1, 'occupied', 12, 2)
                """).update();
        jdbc.sql("""
                INSERT INTO tenants (id, full_name, cpf, owner_id, rent_cents, company_id) VALUES
                  (30, 'Carla Locatária', '98765432100', 10, 150000, 1),
                  (31, 'Locatário de Outra', null, 12, 1, 2)
                """).update();
        jdbc.sql("""
                INSERT INTO contracts (property_id, tenant_id, start_date, status, company_id) VALUES
                  (20, 30, TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', 'active', 1),
                  (22, 30, TIMESTAMP WITH TIME ZONE '2026-02-01 00:00:00+00', 'active', 1)
                """).update();
    }

    @Test
    void healthEPublico() throws Exception {
        mvc.perform(get("/api/v1/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    void loginComSenhaDoSistemaAtualAbreSessaoEAudita() throws Exception {
        var session = login("ADMIN@teste.local", PASSWORD);

        mvc.perform(get("/api/v1/auth/me").cookie(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@teste.local"))
                .andExpect(jsonPath("$.role").value("ADMINISTRADOR"))
                .andExpect(content().string(not(containsString("password"))));

        assertEquals(1, count("SELECT count(*) FROM audit_events WHERE action = 'auth.login' AND user_id = 1"));
        assertEquals(1, count("SELECT count(*) FROM users WHERE id = 1 AND active_company_id = 1"));
    }

    @Test
    void cookieDeSessaoEHttpOnly() throws Exception {
        mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("admin@teste.local", PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly(COOKIE, true))
                .andExpect(header().string("Set-Cookie", containsString("SameSite=Lax")));
    }

    @Test
    void senhaErradaRegistraTentativaEDepoisBloqueia() throws Exception {
        for (int i = 0; i < 10; i++) {
            mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                            .content(credentials("admin@teste.local", "Errada@123")))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error.message").value("Credenciais inválidas ou expiradas."));
        }
        assertEquals(10, count("SELECT count(*) FROM login_attempts WHERE scope = 'email'"));

        // Mesmo com a senha certa: o bloqueio vem antes da verificação.
        mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("admin@teste.local", PASSWORD)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error.code").value("TOO_MANY_ATTEMPTS"));
    }

    @Test
    void usuarioInativoOuSemImobiliariaNaoEntra() throws Exception {
        mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("inativo@teste.local", PASSWORD)))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("semvinculo@teste.local", PASSWORD)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("NO_ACTIVE_COMPANY"));
    }

    @Test
    void acessoTemporarioExpiradoNaoEntra() throws Exception {
        jdbc.sql("UPDATE users SET temporary_access_expires_at = TIMESTAMP WITH TIME ZONE '2020-01-01 00:00:00+00' WHERE id = 1")
                .update();
        mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("admin@teste.local", PASSWORD)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginSemTokenCsrfERecusado() throws Exception {
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("admin@teste.local", PASSWORD)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("CSRF"));
    }

    @Test
    void entradaInvalidaDevolveErroDeValidacao() throws Exception {
        mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(credentials("nao-e-email", "")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.error.requestId").isNotEmpty());
    }

    @Test
    void semSessaoNaoAcessaCadastros() throws Exception {
        mvc.perform(get("/api/v1/owners")).andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("UNAUTHENTICATED"));
        mvc.perform(get("/api/v1/owners").cookie(new Cookie(COOKIE, "token-forjado")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void usuarioDesativadoDepoisDoLoginPerdeASessao() throws Exception {
        var session = login("operacional@teste.local", PASSWORD);
        jdbc.sql("UPDATE users SET status = 'inactive' WHERE id = 2").update();
        mvc.perform(get("/api/v1/owners").cookie(session)).andExpect(status().isUnauthorized());
    }

    @Test
    void sessoesInvalidadasNoSistemaAtualSaoRecusadas() throws Exception {
        var session = login("operacional@teste.local", PASSWORD);
        jdbc.sql("UPDATE users SET sessions_invalidated_at = TIMESTAMP WITH TIME ZONE '2999-01-01 00:00:00+00' WHERE id = 2")
                .update();
        mvc.perform(get("/api/v1/owners").cookie(session)).andExpect(status().isUnauthorized());
    }

    @Test
    void vinculoDesativadoNaImobiliariaDerrubaASessao() throws Exception {
        var session = login("operacional@teste.local", PASSWORD);
        jdbc.sql("UPDATE company_users SET status = 'inactive' WHERE user_id = 2").update();
        mvc.perform(get("/api/v1/owners").cookie(session)).andExpect(status().isUnauthorized());
    }

    @Test
    void locadoresDaImobiliariaAtivaComDocumentoMascarado() throws Exception {
        var session = login("operacional@teste.local", PASSWORD);
        mvc.perform(get("/api/v1/owners").cookie(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.items[0].fullName").value("Ana Locadora"))
                .andExpect(jsonPath("$.items[0].documentMasked").value("***.456.789-**"))
                .andExpect(jsonPath("$.items[0].propertiesCount").value(1))
                .andExpect(jsonPath("$.items[1].documentMasked").value("12.345.678/0001-90"))
                .andExpect(content().string(not(containsString("Outra Imobiliária"))))
                .andExpect(content().string(not(containsString("123.456.789-01"))));
    }

    @Test
    void buscaEPaginacaoDeLocadores() throws Exception {
        var session = login("admin@teste.local", PASSWORD);
        mvc.perform(get("/api/v1/owners").param("q", "BRUNO").cookie(session))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].fullName").value("Bruno Locador"));
        mvc.perform(get("/api/v1/owners").param("size", "1").param("page", "1").cookie(session))
                .andExpect(jsonPath("$.total").value(2))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].fullName").value("Bruno Locador"));
        // Curinga digitado pelo usuário é texto, não padrão.
        mvc.perform(get("/api/v1/owners").param("q", "%").cookie(session))
                .andExpect(jsonPath("$.total").value(0));
        mvc.perform(get("/api/v1/owners").param("size", "5000").cookie(session))
                .andExpect(jsonPath("$.size").value(100));
    }

    @Test
    void imoveisComLocatarioDoContratoAtivoEFiltroDeSituacao() throws Exception {
        var session = login("operacional@teste.local", PASSWORD);
        mvc.perform(get("/api/v1/properties").cookie(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(3))
                .andExpect(jsonPath("$.items[0].code").value("IM-001"))
                .andExpect(jsonPath("$.items[0].ownerName").value("Ana Locadora"))
                .andExpect(jsonPath("$.items[0].tenantName").value("Carla Locatária"))
                .andExpect(jsonPath("$.items[0].rentCents").value(150000))
                .andExpect(jsonPath("$.items[1].tenantName").isEmpty());
        mvc.perform(get("/api/v1/properties").param("status", "vacant").cookie(session))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].code").value("IM-002"));
        mvc.perform(get("/api/v1/properties").param("status", "demolido").cookie(session))
                .andExpect(status().isBadRequest());
    }

    @Test
    void locatarioListaTodosOsLocadoresSemRepetir() throws Exception {
        var session = login("operacional@teste.local", PASSWORD);
        mvc.perform(get("/api/v1/tenants").cookie(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].documentMasked").value("***.654.321-**"))
                .andExpect(jsonPath("$.items[0].ownerNames", hasSize(2)))
                .andExpect(jsonPath("$.items[0].ownerNames[0]").value("Ana Locadora"))
                .andExpect(jsonPath("$.items[0].ownerNames[1]").value("Bruno Locador"));
    }

    @Test
    void outraImobiliariaVeSoOsProprios() throws Exception {
        var session = login("outra@teste.local", PASSWORD);
        mvc.perform(get("/api/v1/properties").cookie(session))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].code").value("X-001"));
    }

    @Test
    void logoutLimpaCookieEAudita() throws Exception {
        var session = login("admin@teste.local", PASSWORD);
        var result = mvc.perform(post("/api/v1/auth/logout").with(csrf()).cookie(session))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge(COOKIE, 0))
                .andReturn();
        assertTrue(result.getResponse().getHeader("Set-Cookie").contains(COOKIE + "="));
        assertEquals(1, count("SELECT count(*) FROM audit_events WHERE action = 'auth.logout'"));
    }

    private Cookie login(String email, String password) throws Exception {
        var result = mvc.perform(post("/api/v1/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(email, password)))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie(COOKIE);
    }

    private static String credentials(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }

    private void user(long id, String email, String status, String role) {
        jdbc.sql("""
                        INSERT INTO users (id, open_id, name, email, role, status, password_hash)
                        VALUES (:id, :openId, :name, :email, :role, :status, :hash)
                        """)
                .param("id", id)
                .param("openId", "local-" + id)
                .param("name", "Usuário " + id)
                .param("email", email)
                .param("role", role == null ? "user" : role)
                .param("status", status)
                .param("hash", HASH)
                .update();
    }

    private void membership(long companyId, long userId, String role) {
        jdbc.sql("INSERT INTO company_users (company_id, user_id, role, status) VALUES (:c, :u, :r, 'active')")
                .param("c", companyId).param("u", userId).param("r", role).update();
    }

    private long count(String sql) {
        return jdbc.sql(sql).query(Long.class).single();
    }
}
