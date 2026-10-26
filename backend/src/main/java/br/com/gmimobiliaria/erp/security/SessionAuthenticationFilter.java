package br.com.gmimobiliaria.erp.security;

import br.com.gmimobiliaria.erp.auth.SessionUserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Lê o cookie de sessão e relê usuário, status e vínculo no banco a cada requisição: desativar o usuário ou invalidar
 * as sessões no sistema atual derruba também a sessão aqui.
 */
public class SessionAuthenticationFilter extends OncePerRequestFilter {
    private final SessionCookies cookies;
    private final SessionTokenService tokens;
    private final SessionUserService sessionUsers;

    public SessionAuthenticationFilter(SessionCookies cookies, SessionTokenService tokens,
                                       SessionUserService sessionUsers) {
        this.cookies = cookies;
        this.tokens = tokens;
        this.sessionUsers = sessionUsers;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        cookies.read(request)
                .flatMap(tokens::verify)
                .flatMap(claims -> sessionUsers.load(claims.userId(), claims.issuedAt()))
                .ifPresent(user -> {
                    var authorities = user.permissions().stream()
                            .map(permission -> new SimpleGrantedAuthority(permission.name()))
                            .toList();
                    var authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
        chain.doFilter(request, response);
    }
}
