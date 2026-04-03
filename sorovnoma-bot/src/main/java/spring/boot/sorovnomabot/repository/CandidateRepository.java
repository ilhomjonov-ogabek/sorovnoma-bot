package spring.boot.sorovnomabot.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import spring.boot.sorovnomabot.entity.Candidate;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

  @Modifying
  @Transactional
  @Query("UPDATE Candidate c SET c.voteCount = c.voteCount + 1 WHERE c.id = :id")
  void incrementVoteCount(@Param("id") Long id);

  @Modifying
  @Transactional
  @Query("UPDATE Candidate c SET c.voteCount = 0 WHERE c.id IN :ids")
  void resetVoteCounts(@Param("ids") List<Long> ids);

}