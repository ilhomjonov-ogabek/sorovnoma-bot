package spring.boot.bankaccountmanagementsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import spring.boot.bankaccountmanagementsystem.enums.TransactionType;

@Getter
@Setter
@Entity
@Table(name = "transaction")
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(nullable = false)
  private TransactionType type;

  @Column(nullable = false)
  private double amount;

  private String description;

}