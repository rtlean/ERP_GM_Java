package br.com.gmimobiliaria.erp.common;

import java.util.List;

/** Corpo de erro definido em {@code docs/contratos-api.md}. */
public record ApiError(Body error) {

    public static ApiError of(String code, String message, List<FieldDetail> details, String requestId) {
        return new ApiError(new Body(code, message, details, requestId));
    }

    public record Body(String code, String message, List<FieldDetail> details, String requestId) {
    }

    public record FieldDetail(String field, String message) {
    }
}
