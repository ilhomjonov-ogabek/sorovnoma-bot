package spring.boot.cardprocessing.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import spring.boot.cardprocessing.entity.IdempotencyKey;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, UUID> {

  Optional<IdempotencyKey> findByIdempotencyKey(String idempotencyKey);


  Optional<IdempotencyKey> findByIdempotencyKeyAndEndpoint(String idempotencyKey, String endpoint);

}