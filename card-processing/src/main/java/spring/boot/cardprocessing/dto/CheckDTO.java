package spring.boot.cardprocessing.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckDTO {

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
