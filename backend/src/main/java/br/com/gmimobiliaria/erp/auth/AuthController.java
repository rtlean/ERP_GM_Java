package br.com.gmimobiliaria.erp.auth;

import br.com.gmimobiliaria.erp.security.CurrentUser;
import br.com.gmimobiliaria.erp.security.Permission;
import br.com.gmimobiliaria.erp.security.SessionCookies;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService auth;
    private final SessionCookies cookies;

    public AuthController(AuthService auth, SessionCookies cookies) {
        this.auth = auth;
        this.cookies = cookies;
    }

    /** Garante o cookie XSRF-TOKEN antes do primeiro POST (o Angular o envia no cabeçalho X-XSRF-TOKEN). */
    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf(CsrfToken token) {
        token.getToken();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public ResponseEntity<SessionUser> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        var result = auth.login(request.email(), request.password(), clientIp(http));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookies.create(result.token()).toString())
                .body(SessionUser.of(result.user()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal CurrentUser user) {
        auth.logout(user);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookies.clear().toString())
                .build();
    }

    @GetMapping("/me")
    public SessionUser me(@AuthenticationPrincipal CurrentUser user) {
        return SessionUser.of(user);
    }

    /** Primeiro endereço de X-Forwarded-For, como no sistema atual (a Vercel fica na frente). */
    static String clientIp(HttpServletRequest request) {
        var forwarded = request.getHeader("X-Forwarded-For");
        var first = forwarded == null ? "" : forwarded.split(",")[0].trim();
        var ip = first.isEmpty() ? request.getRemoteAddr() : first;
        return ip == null ? "" : ip.substring(0, Math.min(ip.length(), 255));
    }

    public record LoginRequest(
            @NotBlank(message = "E-mail é obrigatório.")
            @Email(message = "Formato de e-mail inválido.")
            @Size(max = 320)
            String email,
            @NotBlank(message = "A senha é obrigatória.")
            @Size(min = 6, max = 128, message = "A senha deve ter no mínimo 6 caracteres.")
            String password) {
    }

    /** Lista fechada do que o navegador recebe sobre o usuário logado. */
    public record SessionUser(long id, String name, String email, String role, Set<Permission> permissions,
                              boolean mustChangePassword) {
        static SessionUser of(CurrentUser user) {
            return new SessionUser(user.id(), user.name(), user.email(), user.role().name(), user.permissions(),
                    user.mustChangePassword());
        }
    }
}
