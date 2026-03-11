package spring.boot.cardprocessing.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import spring.boot.cardprocessing.entity.Transaction;
import spring.boot.cardprocessing.enums.TransactionType;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>,
    JpaSpecificationExecutor<Transaction> {

  Page<Transaction> findAllByCard_CardId(UUID cardId, Pageable pageable);

  Optional<Transaction> findByExternalId(String externalId);

  Page<Transaction> findAllByCard_CardIdAndType(UUID cardId, TransactionType type, Pageable pageable);
}