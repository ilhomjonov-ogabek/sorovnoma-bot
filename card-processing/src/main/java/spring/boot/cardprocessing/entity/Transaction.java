package spring.boot.cardprocessing.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;
import spring.boot.cardprocessing.enums.Currency;
import spring.boot.cardprocessing.enums.TransactionPurpose;
import spring.boot.cardprocessing.enums.TransactionType;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(name = "transaction_id", updatable = false, nullable = false)
  private UUID transactionId;

  @Column(name = "external_id", nullable = false)
  private String externalId;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "card_id", nullable = false)

  private Card card;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private TransactionType type;

  @Column(name = "amount", nullable = false)
  private Long amount;

  @Column(name = "after_balance", nullable = false)
  private Long afterBalance;

  @Enumerated(EnumType.STRING)
  @Column(name = "currency", nullable = false)
  private Currency currency;

  @Enumerated(EnumType.STRING)
  @Column(name = "purpose", nullable = true)
  private TransactionPurpose purpose;

  @Column(name = "exchange_rate", nullable = true)
  private Long exchangeRate;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
  }
}