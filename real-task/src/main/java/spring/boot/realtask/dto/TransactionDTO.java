package spring.boot.realtask.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for {@link spring.boot.realtask.entity.TransactionEntity}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO implements Serializable {

  private Long operationId;
  private BigDecimal amount;
  private String currency;
  private String templateId;
  private String terminal;
  private LocalDate date;
  private String senderCard;
  private String receiverCard;
  private BigDecimal commission;
  private String senderName;
  private String receiverName;
}