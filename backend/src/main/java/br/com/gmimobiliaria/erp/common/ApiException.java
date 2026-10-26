package br.com.gmimobiliaria.erp.common;

import org.springframework.http.HttpStatus;

/** Erro de negócio com mensagem segura para exibir ao usuário. */
public class ApiException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }
}
