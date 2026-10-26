package br.com.gmimobiliaria.erp.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import java.io.PrintWriter;
import java.io.StringWriter;

class DatabaseUrlTest {
    @Test
    void erroDeSintaxeNaoExpoeConexaoNemSenhaNaPilha() {
        var input = "postgresql://usuario:[#senha-ficticia]@db.local:5432/postgres";
        var error = assertThrows(IllegalArgumentException.class, () -> DatabaseUrl.parse(input));
        var trace = new StringWriter();
        error.printStackTrace(new PrintWriter(trace));
        assertFalse(trace.toString().contains("senha-ficticia"));
        assertFalse(trace.toString().contains(input));
    }

    @Test
    void aceitaCaracteresEspeciaisCodificadosNaSenha() {
        var url = DatabaseUrl.parse("postgresql://usuario:%23senha%5Bficticia%5D@db.local:5432/postgres");
        assertEquals("#senha[ficticia]", url.password());
    }

    @Test
    void recusaHostInvalidoEFragmento() {
        assertThrows(IllegalArgumentException.class, () -> DatabaseUrl.parse("postgres://u:p@host_invalido/db"));
        assertThrows(IllegalArgumentException.class, () -> DatabaseUrl.parse("postgres://u:p@db.local/db#fragmento"));
    }
    @Test
    void converteUrlDoPoolerDoSupabase() {
        var url = DatabaseUrl.parse("postgresql://postgres.abc:s%40nh%3Aa@aws-0-sa-east-1.pooler.supabase.com:6543/postgres");
        assertEquals("jdbc:postgresql://aws-0-sa-east-1.pooler.supabase.com:6543/postgres"
                + "?sslmode=require&prepareThreshold=0&ApplicationName=erp-gm-java", url.jdbcUrl());
        assertEquals("postgres.abc", url.username());
        assertEquals("s@nh:a", url.password());
    }

    @Test
    void usaPortaEBancoPadrao() {
        var url = DatabaseUrl.parse("postgres://u:p@db.local");
        assertEquals("jdbc:postgresql://db.local:5432/postgres"
                + "?sslmode=require&prepareThreshold=0&ApplicationName=erp-gm-java", url.jdbcUrl());
    }

    @Test
    void naoExpoeSenhaNoToString() {
        assertFalse(DatabaseUrl.parse("postgres://u:segredo@db.local/x").toString().contains("segredo"));
    }

    @Test
    void recusaUrlInvalida() {
        assertThrows(IllegalArgumentException.class, () -> DatabaseUrl.parse(null));
        assertThrows(IllegalArgumentException.class, () -> DatabaseUrl.parse("mysql://u:p@h/db"));
        assertThrows(IllegalArgumentException.class, () -> DatabaseUrl.parse("postgres://h/db"));
    }
}
