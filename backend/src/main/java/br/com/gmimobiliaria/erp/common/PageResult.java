package br.com.gmimobiliaria.erp.common;

import java.util.List;

/** Resposta paginada definida em {@code docs/contratos-api.md}. {@code page} começa em 0. */
public record PageResult<T>(List<T> items, int page, int size, long total) {
}
