package spring.boot.realtask.exceptions;


import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiResponseErrorException.class)
  public ResponseEntity<?> handleApiResponseErrorException(ApiResponseErrorException e) {
    log.warn("ApiResponseErrorException : {}", e.getMessage());
    return ResponseEntity
        .status(HttpStatus.BAD_GATEWAY)
        .body(e.getMessage());
  }

}
