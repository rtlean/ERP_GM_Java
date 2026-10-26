package br.com.gmimobiliaria.erp.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Converte a {@code DATABASE_URL} no formato usado pelo sistema atual ({@code postgres://usuario:senha@host:porta/banco})
 * para os parâmetros JDBC. Assim o novo backend usa a mesma variável, sem duplicar credenciais.
 */
public record DatabaseUrl(String jdbcUrl, String username, String password) {

    public static DatabaseUrl parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("DATABASE_URL não informada.");
        }
        final URI uri;
        try {
            uri = URI.create(value.trim());
        } catch (IllegalArgumentException exception) {
            // A causa original inclui a URL completa, inclusive a senha. Não propagá-la.
            throw new IllegalArgumentException("DATABASE_URL inválida. Codifique os caracteres especiais das credenciais para uso em URL.");
        }
        var scheme = uri.getScheme();
        if (!"postgres".equals(scheme) && !"postgresql".equals(scheme)) {
            throw new IllegalArgumentException("DATABASE_URL deve usar o esquema postgres:// ou postgresql://.");
        }
        if (uri.getHost() == null || uri.getFragment() != null) {
            throw new IllegalArgumentException("DATABASE_URL inválida: confira o host e a codificação das credenciais.");
        }
        var userInfo = uri.getRawUserInfo();
        if (userInfo == null || !userInfo.contains(":")) {
            throw new IllegalArgumentException("DATABASE_URL deve conter usuário e senha.");
        }
        var separator = userInfo.indexOf(':');
        var username = decode(userInfo.substring(0, separator));
        var password = decode(userInfo.substring(separator + 1));
        var port = uri.getPort() > 0 ? uri.getPort() : 5432;
        var database = uri.getPath() == null || uri.getPath().length() <= 1 ? "postgres" : uri.getPath().substring(1);

        // prepareThreshold=0: o pooler do Supabase em modo transação não suporta prepared statements nomeados.
        var jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + "/" + database
                + "?sslmode=require&prepareThreshold=0&ApplicationName=erp-gm-java";
        return new DatabaseUrl(jdbcUrl, username, password);
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return "DatabaseUrl[jdbcUrl=" + jdbcUrl + ", username=" + username + ", password=***]";
    }
}
