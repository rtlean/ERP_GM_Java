package br.com.gmimobiliaria.erp.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LegacyPasswordHasherTest {
    /** Gerado com o Node do sistema atual: scryptSync("Senha@123", salt, 64). */
    static final String NODE_HASH = "0123456789abcdef0123456789abcdef:"
            + "72fb9f406c870be54290efe23dfdab096d60a4181d2440ee3e0bbd80e8e72178"
            + "b2dde38c6484a23be70f3bb3b092c3d5592a9fdfd7c5437f570ca5c21afe866b";

    private final LegacyPasswordHasher hasher = new LegacyPasswordHasher();

    @Test
    void aceitaHashGeradoPeloSistemaAtual() {
        assertTrue(hasher.matches("Senha@123", NODE_HASH));
    }

    @Test
    void recusaSenhaErrada() {
        assertFalse(hasher.matches("Senha@124", NODE_HASH));
    }

    @Test
    void recusaHashMalformadoOuAusente() {
        assertFalse(hasher.matches("Senha@123", null));
        assertFalse(hasher.matches("Senha@123", ""));
        assertFalse(hasher.matches("Senha@123", "sem-separador"));
        assertFalse(hasher.matches("Senha@123", "salt:nao-e-hex"));
        assertFalse(hasher.matches("Senha@123", "salt:"));
        assertFalse(hasher.matches(null, NODE_HASH));
    }

    @Test
    void hashNovoUsaOMesmoFormato() {
        var encoded = hasher.hash("Outra@Senha1");
        assertTrue(encoded.matches("[0-9a-f]{32}:[0-9a-f]{128}"));
        assertTrue(hasher.matches("Outra@Senha1", encoded));
    }
}
