package spring.boot.sorovnomabot.service.impl;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import spring.boot.sorovnomabot.entity.Poll;
import spring.boot.sorovnomabot.repository.PollRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class PollScheduler {

  private final PollRepository pollRepository;

  @Scheduled(cron = "0 0 0 * * *")
  public void finishExpiredPolls() {
    log.info("Muddati o'tgan so'rovnomalar tekshirilmoqda...");

    List<Poll> expiredPolls = pollRepository
        .findByActiveAndFinishedDateBefore(true, LocalDate.now());

    for (Poll poll : expiredPolls) {
      poll.setActive(false);
      pollRepository.save(poll);
      log.info("So'rovnoma avtomatik yakunlandi: pollId={}, title={}",
          poll.getId(), poll.getTitle());
    }

    log.info("Jami {} ta so'rovnoma to'xtatildi", expiredPolls.size());
  }
}
