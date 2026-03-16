package spring.boot.realtask.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import spring.boot.realtask.entity.TransactionEntity;

public interface TransactionEntityRepository extends JpaRepository<TransactionEntity, Long> {

  Optional<TransactionEntity> findByOperationId(Long operationId);
}