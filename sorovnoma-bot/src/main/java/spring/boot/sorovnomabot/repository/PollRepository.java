package spring.boot.sorovnomabot.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import spring.boot.sorovnomabot.entity.Poll;

public interface PollRepository extends JpaRepository<Poll, Long> {

  List<Poll> findByActive(boolean active);

  List<Poll> findByActiveAndFinishedDateBefore(boolean b, LocalDate now);
}