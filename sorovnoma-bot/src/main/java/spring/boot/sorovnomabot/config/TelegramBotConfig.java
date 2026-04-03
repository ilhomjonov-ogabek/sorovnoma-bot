package spring.boot.sorovnomabot.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.Serializable;
import java.util.List;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import spring.boot.sorovnomabot.enums.RequestEnum;
import spring.boot.sorovnomabot.service.BotResponseService;
import spring.boot.sorovnomabot.service.impl.AllMethodsService;
import org.telegram.telegrambots.bots.DefaultBotOptions;

@Slf4j
@Component
public class TelegramBotConfig extends TelegramLongPollingBot {

  private final AllMethodsService allMethodsService;
  private final BotResponseService botResponseService;


  public TelegramBotConfig(AllMethodsService allMethodsService,
      @Lazy BotResponseService botResponseService) {
    super(new DefaultBotOptions());
    this.allMethodsService = allMethodsService;
    this.botResponseService = botResponseService;

  }


  @Value("${bot.username}")
  private String botUsername;

  @Value("${bot.token}")
  private String botToken;

  @Override
  public String getBotUsername() {
    return botUsername;
  }

  @Override
  public String getBotToken() {
    return botToken;
  }

  @SneakyThrows
  @Override
  public void onUpdateReceived(Update update) {

    RequestEnum request = allMethodsService.requestCheckReturnEnum(update);

    switch (request) {
      case START -> botResponseService.pressStart(update);
      case SIMPLE_START -> execute(botResponseService.pressSimpleStart(update));
      case SEND_POLLS -> execute(botResponseService.pressActivePolls(update));
      case GET_RESULT -> execute(botResponseService.pressGetResult(update));
      case INFORMATION -> execute(botResponseService.pressInformation(update));
      case VOTE -> execute(botResponseService.pressVote(update));
      case ADMIN -> execute(botResponseService.pressAdminPage(update));
      case ACTIVE_POLLS -> execute(botResponseService.pressActivePolls(update));
      case POLL -> execute(botResponseService.pressPoll(update));
      case FINISH_POLL -> execute(botResponseService.pressFinishPoll(update));
      case GET_RESULT_EXCEL -> execute(botResponseService.pressGetResultExcel(update));
      case CREATE_POLL -> execute(botResponseService.pressCreatePoll(update));
      case POLL_CREATION -> execute(botResponseService.handlePollCreation(update));
      case PASSIVE_POLLS -> execute(botResponseService.pressPassivePolls(update));
      case PICK_POLL -> execute(botResponseService.pressPickPoll(update));
      case FINISHING_POLL -> execute(botResponseService.pressFinishingPoll(update));
      case EXPORT -> execute(botResponseService.pressExportPoll(update));
      case PICK_CLEAR_VOTES -> execute(botResponseService.pressPickClearVotes(update));
      case CLEAR_VOTES -> execute(botResponseService.pressClearVotes(update));
      case ADD_ADMIN -> execute(botResponseService.pressAddAdmin(update));
      case NEW_ADMIN -> execute(botResponseService.newAdmin(update));
      case REMOVE_ADMIN -> execute(botResponseService.pressRemoveAdmin(update));
      case REMOVED_ADMIN -> execute(botResponseService.removedAdmin(update));
      case APPROVE_POLL -> execute(botResponseService.approvePoll(update));
      case DEFAULT -> execute(botResponseService.defaultMessage(update));
      case SUBSCRIBE -> execute(botResponseService.pressSubscribe(update));
      case NOT_APPROVED_POLL -> execute(botResponseService.pressNotApprovedPoll(update));
    }

  }

  @Override
  public <T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method)
      throws TelegramApiException {
    try {
      java.lang.reflect.Field field = org.telegram.telegrambots.bots.DefaultAbsSender.class.getDeclaredField(
          "objectMapper");
      field.setAccessible(true);

      if (field.get(this) == null) {
        field.set(this, new com.fasterxml.jackson.databind.ObjectMapper());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return super.execute(method);
  }

  @PostConstruct
  public void onStart() {
    List<Long> adminsChatId = botResponseService.getAdminsChatId();
    for (Long chatId : adminsChatId) {
      try {
        execute(botResponseService.sendToAdminsStart(chatId,"✅ Bot ishga tushdi!"));
      } catch (TelegramApiException e) {
        log.error("Adminga ✅ Bot ishga tushdi! messageda error:"+e.getMessage());
      }

    }
  }

  @PreDestroy
  public void onStop() {
    List<Long> adminsChatId = botResponseService.getAdminsChatId();
    for (Long chatId : adminsChatId) {
      try {
        execute(botResponseService.sendToAdminsStop(chatId,"🔴 Bot o'chirilmoqda..."));
      } catch (TelegramApiException e) {
        log.error("Adminga 🔴 Bot o'chirilmoqda... messageda error:"+e.getMessage());
      }
    }
  }
}
