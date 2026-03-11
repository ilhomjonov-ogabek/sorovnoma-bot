package spring.boot.cardprocessing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {

  @JsonProperty("code")
  private String code;

  @JsonProperty("message")
  private String message;


  public static ErrorResponse missingField(String fields) {
    return new ErrorResponse("missing_field", "Missing required field(s): " + fields);
  }

  public static ErrorResponse invalidData(String message) {
    return new ErrorResponse("invalid_data", message);
  }

  public static ErrorResponse limitExceeded(String message) {
    return new ErrorResponse("limit_exceeded", message);
  }

  public static ErrorResponse insufficientFunds() {
    return new ErrorResponse("insufficient_funds", "Card balance is not enough");
  }

  public static ErrorResponse notFound(String message) {
    return new ErrorResponse("not_found", message);
  }

  public static ErrorResponse unauthorized() {
    return new ErrorResponse("unauthorized", "Authorization token not exists.");
  }

  public static ErrorResponse incompatibleStatus(String message) {
    return new ErrorResponse("incompatible_status", message);
  }
}
