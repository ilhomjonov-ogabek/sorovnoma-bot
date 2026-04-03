package spring.boot.sorovnomabot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotInitializer {

  private final TelegramBotConfig telegramBotConfig;

  public BotInitializer(TelegramBotConfig telegramBotConfig) {
    this.telegramBotConfig = telegramBotConfig;
  }

  @Bean
  public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
    TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
    api.registerBot(telegramBotConfig);
    return api;
  }
}
