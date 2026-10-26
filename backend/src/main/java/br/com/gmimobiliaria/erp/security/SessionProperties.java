package br.com.gmimobiliaria.erp.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("erp.session")
public record SessionProperties(String secret, String cookieName, Duration ttl, boolean secureCookie) {
}
