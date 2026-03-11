package spring.boot.cardprocessing.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import spring.boot.cardprocessing.entity.Card;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

  List<Card> findAllByUserId(Long userId);


  @Query("SELECT COUNT(c) FROM Card c WHERE c.userId = :userId AND c.status != 'CLOSED'")
  long countNonClosedCardsByUserId(@Param("userId") Long userId);


  Optional<Card> findByCardIdAndUserId(UUID cardId, Long userId);
}