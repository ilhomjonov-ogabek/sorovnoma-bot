package spring.boot.sorovnomabot.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import spring.boot.sorovnomabot.service.BotRequestService;

@Service
@RequiredArgsConstructor
public class BotRequestServiceImpl implements BotRequestService {

  private final PollCreationStateHolder pollCreationStateHolder;

  private boolean isMessage(Update u) {
    return u != null && u.hasMessage()
        && !u.getMessage().hasAudio()
        && !u.getMessage().hasVideo()
        && !u.getMessage().hasContact()
        && !u.getMessage().hasLocation()
        && !u.getMessage().hasSticker()
        && !u.getMessage().hasInvoice()
        && !u.getMessage().hasVoice()
        && !u.getMessage().hasVideoNote()
        && !u.getMessage().hasDocument()
        && !u.getMessage().hasPoll();
  }

  private boolean isCallback(Update u) {
    return u != null && u.hasCallbackQuery();
  }


  @Override
  public boolean equalsStart(Update update) {
    if(!isMessage(update)) {
      return false;
    }

    return update.getMessage().hasText()
        && update.getMessage().getText().length()>6
        && update.getMessage().getText().startsWith("/start");

  }

  @Override
  public boolean equalsVote(Update update) {
    if(!isCallback(update)) {
      return false;
    }
    if (!update.getCallbackQuery().getData().contains("#")){
      return false;
    }

    String[] split = update.getCallbackQuery().getData().split("#");
    return update.getCallbackQuery().getMessage().hasPhoto()
        && split[0].equals("vote");
  }

  @Override
  public boolean equalsAdminMenu(Update update) {

    if(!isMessage(update)) {
      return false;
    }

    return update.getMessage().hasText()
        && update.getMessage().getText().equals("\uD83D\uDC68\uD83C\uDFFB\u200D\uD83D\uDCBBAdmin Panel");
  }

  @Override
  public boolean equalsActivePolls(Update update) {
    if(!isCallback(update)) {
      return false;
    }
    return update.getCallbackQuery().getData().equals("active_polls");
  }

  @Override
  public boolean pressPoll(Update update) {

    if(!isCallback(update) ) {
      return false;
    }

    if (update.getCallbackQuery().getData().contains("#")){
      return false;
    }

    return update.getCallbackQuery().getData().startsWith("poll_");
  }

  @Override
  public boolean pressFinishPoll(Update update) {
    if(!isCallback(update)) {
      return false;
    }
    return update.getCallbackQuery().getData().startsWith("finish_poll");
  }

  @Override
  public boolean pressGetResultExcel(Update update) {
    if(!isCallback(update)) {
      return false;
    }
    return update.getCallbackQuery().getData().startsWith("get_result");
  }

  @Override
  public boolean pressCreatePoll(Update update) {
    if(!isMessage(update)||pollCreationStateHolder.containsKey(update.getMessage().getChatId())) {
      return false;
    }
    return update.getMessage().getText().equals("➕Yangi so'rovnoma");
  }

  @Override
  public boolean isInPollCreation(Update update) {
    if (!isMessage(update)) {
      return false;
    }
    return pollCreationStateHolder.containsKey(update.getMessage().getChatId())
        && (update.getMessage().hasPhoto() || update.getMessage().hasText());
  }

  @Override
  public boolean pressPassivePolls(Update update) {
    if(!isMessage(update)) {
      return false;
    }
    return update.getMessage().getText().equals("\uD83D\uDCDA Arxiv")||update.getMessage().getText().equals("\uD83D\uDCCB Batafsil hisobot");
  }

  @Override
  public boolean pressSimpleStart(Update update) {
    if (!isMessage(update)) {
      return false;
    }
    return (update.getMessage().getText().equals("/start")||update.getMessage().getText().equals("\uD83D\uDD1A Orqaga"));
  }

  @Override
  public boolean equalsSendPolls(Update update) {
    if (!isMessage(update)) {
      return false;
    }
    return update.getMessage().getText().equals("\uD83D\uDDF3\uFE0F Ovoz berish");
  }

  @Override
  public boolean pickPoll(Update update) {
    if (!isCallback(update)) {
      return false;
    }
    return update.getCallbackQuery().getData().startsWith("pick_poll");
  }

  @Override
  public boolean pressGetResult(Update update) {
    if (!isMessage(update)) {
      return false;
    }
    return update.getMessage().getText().equals("\uD83D\uDCCA Natijalar");
  }

  @Override
  public boolean pressInformation(Update update) {
    if (!isMessage(update)) {
      return false;
    }
    return update.getMessage().getText().equals("ℹ\uFE0F Ma'lumot");
  }

  @Override
  public boolean pressFinishingPoll(Update update) {
    if (!isMessage(update)) {
      return false;
    }
    return update.getMessage().getText().equals("⏸\uFE0F Konkursni to'xtatish");
  }

  @Override
  public boolean pressExportPoll(Update update) {
    if (!isMessage(update)) {
      return false;
    }
    return update.getMessage().getText().equals("\uD83D\uDCE5 Eksport");
  }

  @Override
  public boolean pressClearVotes(Update update) {
    if (!isMessage(update)) {
      return false;
    }
    return update.getMessage().getText().equals("\uD83D\uDDD1 Ovozlarni tozalash");

  }

  @Override
  public boolean equalsClearVotes(Update update) {
    if (!isCallback(update)) {
      return false;
    }
    return update.getCallbackQuery().getData().startsWith("clear_votes");
  }

  @Override
  public boolean pressAddAdmin(Update update) {
    if (!isMessage(update)) {
      return false;
    }
    return update.getMessage().getText().equals("Admin qo'shish➕");
  }

  @Override
  public boolean equalsNewAdmin(Update update) {
    if (!isMessage(update)) {
      return false;
    }
    return update.getMessage().getText().matches("@[a-zA-Z][a-zA-Z0-9_]{2,}");
  }

  @Override
  public boolean pressRemovedAdmin(Update update) {
    if (!isMessage(update)) {
      return false;
    }
    return update.getMessage().getText().matches("remove@[a-zA-Z][a-zA-Z0-9_]{2,}");
  }

  @Override
  public boolean pressRemoveAdmin(Update update) {
    if (!isMessage(update)) {
      return false;
    }
    return update.getMessage().getText().equals("Admin o'chirish⛔");
  }

  @Override
  public boolean pressApprove(Update update) {
    if (!isCallback(update)) {
      return false;
    }

    return update.getCallbackQuery().getData().startsWith("approve");
  }

  @Override
  public boolean equalsPhoto(Update update) {
    if (isCallback(update)) {
      return false;
    }
    if (!update.getMessage().hasPhoto()){
      return false;
    }

    return update.getMessage().hasPhoto() && !pollCreationStateHolder.containsKey(update.getMessage().getChatId());
  }

  @Override
  public boolean pressSubscribe(Update update) {
    if (!isCallback(update)) {
      return false;
    }
    return update.getCallbackQuery().getData().startsWith("subscribe");
  }

  @Override
  public boolean pressNotApproved(Update update) {
    if (!isCallback(update)) {
      return false;
    }
    return update.getCallbackQuery().getData().equals("notApproved");
  }

}
