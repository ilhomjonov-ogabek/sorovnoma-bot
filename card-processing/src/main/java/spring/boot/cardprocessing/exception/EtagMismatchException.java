package spring.boot.cardprocessing.exception;

public class EtagMismatchException extends RuntimeException {

  public EtagMismatchException(String message) {
    super(message);
  }
}
