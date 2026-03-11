package spring.boot.cardprocessing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import spring.boot.cardprocessing.enums.CardStatus;
import spring.boot.cardprocessing.enums.Currency;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(name = "card_id", updatable = false, nullable = false)
  private UUID cardId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private CardStatus status;

  @Column(name = "balance", nullable = false)
  private Long balance;

  @Enumerated(EnumType.STRING)
  @Column(name = "currency", nullable = false)
  private Currency currency;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @OneToMany(mappedBy = "card", fetch = FetchType.LAZY)
  private List<Transaction> transactions;

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
    if (this.status == null) this.status = CardStatus.ACTIVE;
    if (this.balance == null) this.balance = 0L;
    if (this.currency == null) this.currency = Currency.UZS;
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = LocalDateTime.now();
  }
}


