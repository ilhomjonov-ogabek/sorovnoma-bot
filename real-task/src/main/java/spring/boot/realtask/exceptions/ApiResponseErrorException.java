package spring.boot.realtask.exceptions;

public class ApiResponseErrorException extends RuntimeException {

  public ApiResponseErrorException(String message) {
    super(message);
  }
}
