package spring.boot.cardprocessing.exception;

public class IncompatibleStatusException extends RuntimeException {

  public IncompatibleStatusException(String message) {
    super(message);
  }
}
