package spring.boot.bankaccountmanagementsystem.dto;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for {@link spring.boot.bankaccountmanagementsystem.entity.Account}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTO implements Serializable {

  private UUID userId;
  private double balance;
}