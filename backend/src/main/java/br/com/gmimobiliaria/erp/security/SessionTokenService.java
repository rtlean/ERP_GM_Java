package br.com.gmimobiliaria.erp.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Token de sessão do novo sistema. É independente do cookie do sistema atual: cada um assina com o próprio segredo.
 * O token só carrega o id do usuário; perfil e status são relidos do banco a cada requisição.
 */
@Component
public class SessionTokenService {
    private static final String ISSUER = "erp-gm-java";
    private static final int MIN_SECRET_LENGTH = 32;

    private final SessionProperties properties;
    private final Clock clock;
    private final byte[] secret;

    public SessionTokenService(SessionProperties properties, Clock clock) {
        if (properties.secret() == null || properties.secret().length() < MIN_SECRET_LENGTH) {
            throw new IllegalStateException("SESSION_SECRET deve ter ao menos " + MIN_SECRET_LENGTH + " caracteres.");
        }
        this.properties = properties;
        this.clock = clock;
        this.secret = properties.secret().getBytes(StandardCharsets.UTF_8);
    }

    public String issue(long userId) {
        var now = clock.instant();
        var claims = new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .subject(Long.toString(userId))
                .jwtID(UUID.randomUUID().toString())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plus(properties.ttl())))
                .build();
        try {
            var jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
            jwt.sign(new MACSigner(secret));
            return jwt.serialize();
        } catch (JOSEException error) {
            throw new IllegalStateException("Falha ao assinar a sessão.", error);
        }
    }

    public Optional<SessionClaims> verify(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            var jwt = SignedJWT.parse(token);
            if (!JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm()) || !jwt.verify(new MACVerifier(secret))) {
                return Optional.empty();
            }
            var claims = jwt.getJWTClaimsSet();
            var now = clock.instant();
            if (!ISSUER.equals(claims.getIssuer()) || claims.getExpirationTime() == null
                    || claims.getIssueTime() == null || !now.isBefore(claims.getExpirationTime().toInstant())) {
                return Optional.empty();
            }
            return Optional.of(new SessionClaims(Long.parseLong(claims.getSubject()), claims.getIssueTime().toInstant()));
        } catch (ParseException | JOSEException | NumberFormatException invalid) {
            return Optional.empty();
        }
    }

    public record SessionClaims(long userId, Instant issuedAt) {
    }
}
