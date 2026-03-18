package spring.boot.cardprocessing.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import java.util.stream.Collectors;
import spring.boot.cardprocessing.dto.ErrorResponse;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

  @ExceptionHandler(CardNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleCardNotFound(CardNotFoundException ex) {
    log.warn("Card not found: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ErrorResponse.notFound(ex.getMessage()));
  }

  @ExceptionHandler(LimitExceededException.class)
  public ResponseEntity<ErrorResponse> handleLimitExceeded(LimitExceededException ex) {
    log.warn("Limit exceeded: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.limitExceeded(ex.getMessage()));
  }

  @ExceptionHandler(InsufficientFundsException.class)
  public ResponseEntity<ErrorResponse> handleInsufficientFunds(InsufficientFundsException ex) {
    log.warn("Insufficient funds: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.insufficientFunds());
  }


  @ExceptionHandler(IncompatibleStatusException.class)
  public ResponseEntity<ErrorResponse> handleIncompatibleStatus(IncompatibleStatusException ex) {
    log.warn("Incompatible status: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.incompatibleStatus(ex.getMessage()));
  }


  @ExceptionHandler(EtagMismatchException.class)
  public ResponseEntity<ErrorResponse> handleEtagMismatch(EtagMismatchException ex) {
    log.warn("ETag mismatch: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.PRECONDITION_FAILED)
        .body(ErrorResponse.invalidData(ex.getMessage()));
  }


  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    String fields = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(FieldError::getField)
        .collect(Collectors.joining(", "));

    log.warn("Validation failed for fields: {}", fields);

    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.missingField(fields));
  }


  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
    log.warn("Missing header: {}", ex.getHeaderName());
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.missingField(ex.getHeaderName()));
  }

  @ExceptionHandler(CheckGenerationException.class)
  public ResponseEntity<ErrorResponse> handleCheckGeneration(CheckGenerationException ex) {
    log.warn("Check generation: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.BAD_GATEWAY)
        .body(ErrorResponse.missingField(ex.getMessage()));
  }

  @ExceptionHandler(CbuServiceException.class)
  public ResponseEntity<ErrorResponse> handleCbuApiError(CbuServiceException ex) {
    log.warn("Cbu service error: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.BAD_GATEWAY)
        .body(ErrorResponse.missingField(ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
    log.error("Unexpected error: {}", ex.getMessage(), ex);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("internal_error", "An unexpected error occurred"));
  }
}
