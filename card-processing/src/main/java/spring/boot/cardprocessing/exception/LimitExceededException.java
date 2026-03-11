package spring.boot.cardprocessing.exception;

public class LimitExceededException extends RuntimeException {

  public LimitExceededException(String message) {
    super(message);
  }
}
