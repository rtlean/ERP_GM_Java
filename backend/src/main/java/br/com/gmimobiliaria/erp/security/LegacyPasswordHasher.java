package br.com.gmimobiliaria.erp.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;
import org.bouncycastle.crypto.generators.SCrypt;
import org.springframework.stereotype.Component;

/**
 * Mesmo formato de {@code users.password_hash} do sistema atual: {@code <salt-hex>:<hash-hex>}, com
 * {@code scryptSync(senha, salt, 64)} do Node (N=16384, r=8, p=1). O salt entra como texto, não como bytes do hex.
 * Os dois sistemas leem a mesma coluna, então o formato não pode divergir.
 */
@Component
public class LegacyPasswordHasher {
    private static final int COST = 16384;
    private static final int BLOCK_SIZE = 8;
    private static final int PARALLELISM = 1;
    private static final int KEY_LENGTH = 64;
    private static final HexFormat HEX = HexFormat.of();
    private final SecureRandom random = new SecureRandom();

    public boolean matches(String password, String encoded) {
        if (password == null || encoded == null) {
            return false;
        }
        var separator = encoded.indexOf(':');
        if (separator <= 0 || separator == encoded.length() - 1) {
            return false;
        }
        var salt = encoded.substring(0, separator);
        byte[] expected;
        try {
            expected = HEX.parseHex(encoded.substring(separator + 1));
        } catch (IllegalArgumentException invalidHex) {
            return false;
        }
        var actual = derive(password, salt);
        return expected.length == actual.length && MessageDigest.isEqual(actual, expected);
    }

    public String hash(String password) {
        var saltBytes = new byte[16];
        random.nextBytes(saltBytes);
        var salt = HEX.formatHex(saltBytes);
        return salt + ":" + HEX.formatHex(derive(password, salt));
    }

    private static byte[] derive(String password, String salt) {
        return SCrypt.generate(
                password.getBytes(StandardCharsets.UTF_8),
                salt.getBytes(StandardCharsets.UTF_8),
                COST, BLOCK_SIZE, PARALLELISM, KEY_LENGTH);
    }
}
