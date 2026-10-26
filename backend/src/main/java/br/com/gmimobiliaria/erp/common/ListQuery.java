package br.com.gmimobiliaria.erp.common;

import java.util.Locale;

/** Busca e paginação já normalizadas. O limite máximo protege o banco compartilhado com o sistema atual. */
public record ListQuery(String search, int page, int size) {
    public static final int DEFAULT_SIZE = 25;
    public static final int MAX_SIZE = 100;
    private static final int MAX_SEARCH_LENGTH = 120;

    public static ListQuery of(String search, Integer page, Integer size) {
        var normalized = search == null ? "" : search.trim();
        if (normalized.length() > MAX_SEARCH_LENGTH) {
            normalized = normalized.substring(0, MAX_SEARCH_LENGTH);
        }
        var safePage = page == null || page < 0 ? 0 : page;
        var safeSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        return new ListQuery(normalized.isEmpty() ? null : normalized, safePage, safeSize);
    }

    public boolean hasSearch() {
        return search != null;
    }

    /** Padrão para ILIKE com os curingas do usuário escapados. */
    public String likePattern() {
        var escaped = search.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

    public int offset() {
        return page * size;
    }
}
