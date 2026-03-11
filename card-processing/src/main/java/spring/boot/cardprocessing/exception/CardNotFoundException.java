package spring.boot.cardprocessing.exception;

// ===== Karta topilmadi =====
public class CardNotFoundException extends RuntimeException {
  public CardNotFoundException(String message) {
    super(message);
  }
}
