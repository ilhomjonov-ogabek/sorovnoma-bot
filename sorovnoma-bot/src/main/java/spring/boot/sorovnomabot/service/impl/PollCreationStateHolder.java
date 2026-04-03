package spring.boot.sorovnomabot.service.impl;

import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import spring.boot.sorovnomabot.enums.PollCreateState;

@Component
public class PollCreationStateHolder {
  private final Map<Long, PollCreateState> states = new HashMap<>();

  public boolean containsKey(Long chatId) {
    return states.containsKey(chatId);
  }

  public void put(Long chatId, PollCreateState state) {
    states.put(chatId, state);
  }

  public PollCreateState get(Long chatId) {
    return states.get(chatId);
  }

  public void remove(Long chatId) {
    states.remove(chatId);
  }
}
