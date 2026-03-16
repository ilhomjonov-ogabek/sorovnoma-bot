package spring.boot.returnjson;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class DTO {
  private Long operationId;
  private String templateId;
  private BigDecimal amount;
  private String currency;
  private String terminal;
  private LocalDate date;
  private String senderCard;
  private String receiverCard;
  private BigDecimal commission;
  private String senderName;
  private String receiverName;


}

