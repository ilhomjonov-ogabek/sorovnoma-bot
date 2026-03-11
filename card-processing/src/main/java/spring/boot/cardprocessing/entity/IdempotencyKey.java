package spring.boot.cardprocessing.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "idempotency_keys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyKey {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(name = "id", updatable = false, nullable = false)
  private UUID id;

  @Column(name = "idempotency_key", nullable = false, unique = true)
  private String idempotencyKey;

  @Column(name = "endpoint", nullable = false)
  private String endpoint;

  @Column(name = "response_body", nullable = false, columnDefinition = "TEXT")
  private String responseBody;

  @Column(name = "status_code", nullable = false)
  private Integer statusCode;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @PrePersist
  public void prePersist() {
    this.createdAt = LocalDateTime.now();
  }
}
