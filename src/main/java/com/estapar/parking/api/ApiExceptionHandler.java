package com.estapar.parking.api;

import com.estapar.parking.api.dto.response.ErrorResponse;
import com.estapar.parking.config.CorrelationIdFilter;
import com.estapar.parking.domain.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @ExceptionHandler({
    MethodArgumentNotValidException.class,
    BindException.class,
    ConstraintViolationException.class,
    HttpMessageNotReadableException.class
  })
  public ResponseEntity<ErrorResponse> handleValidation(Exception ex, HttpServletRequest request) {
    log.warn("erro de validacao: {}", ex.getMessage());
    return buildResponse("VALIDATION_ERROR", ex.getMessage(), HttpStatus.BAD_REQUEST, request);
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusiness(
      BusinessException ex, HttpServletRequest request) {
    log.warn("regra de negocio: {}", ex.getMessage());
    return buildResponse("BUSINESS_ERROR", ex.getMessage(), HttpStatus.UNPROCESSABLE_ENTITY, request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
    log.error("erro inesperado", ex);
    return buildResponse("INTERNAL_ERROR", "Erro interno", HttpStatus.INTERNAL_SERVER_ERROR, request);
  }

  private ResponseEntity<ErrorResponse> buildResponse(
      String code, String message, HttpStatus status, HttpServletRequest request) {
    var traceId = MDC.get(CorrelationIdFilter.CORRELATION_ID_KEY);
    var error =
        new ErrorResponse(code, message, OffsetDateTime.now(), request.getRequestURI(), traceId);
    return ResponseEntity.status(status).body(error);
  }
}
