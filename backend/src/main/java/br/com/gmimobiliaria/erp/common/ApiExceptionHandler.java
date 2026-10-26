package br.com.gmimobiliaria.erp.common;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiError> handleApi(ApiException error, HttpServletRequest request) {
        return respond(error.status(), error.code(), error.getMessage(), List.of(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException error, HttpServletRequest request) {
        var details = error.getBindingResult().getFieldErrors().stream()
                .map(field -> new ApiError.FieldDetail(field.getField(), field.getDefaultMessage()))
                .toList();
        return respond(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Verifique os campos informados.", details, request);
    }

    @ExceptionHandler({HandlerMethodValidationException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiError> handleParameters(Exception error, HttpServletRequest request) {
        return respond(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Parâmetros inválidos.", List.of(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> handleDenied(AccessDeniedException error, HttpServletRequest request) {
        return respond(HttpStatus.FORBIDDEN, "FORBIDDEN",
                "Você não possui permissão para acessar esta funcionalidade.", List.of(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException error, HttpServletRequest request) {
        return respond(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Requisição inválida.", List.of(), request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception error, HttpServletRequest request) {
        // Exceções do Spring MVC (rota inexistente, método não suportado...) já trazem o status correto.
        if (error instanceof ErrorResponse framework && framework.getStatusCode().is4xxClientError()) {
            var status = HttpStatus.valueOf(framework.getStatusCode().value());
            return respond(status, status.name(), "Requisição não atendida.", List.of(), request);
        }
        log.error("Erro inesperado [requestId={}]", RequestIdFilter.of(request), error);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Não foi possível concluir a operação. Tente novamente.", List.of(), request);
    }

    private static ResponseEntity<ApiError> respond(HttpStatus status, String code, String message,
                                                    List<ApiError.FieldDetail> details, HttpServletRequest request) {
        return ResponseEntity.status(status).body(ApiError.of(code, message, details, RequestIdFilter.of(request)));
    }
}
