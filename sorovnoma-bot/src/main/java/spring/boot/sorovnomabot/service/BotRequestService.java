package spring.boot.sorovnomabot.service;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface BotRequestService {

  boolean equalsStart(Update update);

  boolean equalsVote(Update update);

  boolean equalsAdminMenu(Update update);

  boolean equalsActivePolls(Update update);

  boolean pressPoll(Update update);

  boolean pressFinishPoll(Update update);

  boolean pressGetResultExcel(Update update);

  boolean pressCreatePoll(Update update);

  boolean isInPollCreation(Update update);

  boolean pressPassivePolls(Update update);

  boolean pressSimpleStart(Update update);

  boolean equalsSendPolls(Update update);

  boolean pickPoll(Update update);

  boolean pressGetResult(Update update);

  boolean pressInformation(Update update);

  boolean pressFinishingPoll(Update update);

  boolean pressExportPoll(Update update);

  boolean pressClearVotes(Update update);

  boolean equalsClearVotes(Update update);

  boolean pressAddAdmin(Update update);

  boolean equalsNewAdmin(Update update);

  boolean pressRemovedAdmin(Update update);

  boolean pressRemoveAdmin(Update update);

  boolean pressApprove(Update update);

  boolean equalsPhoto(Update update);

  boolean pressSubscribe(Update update);

  boolean pressNotApproved(Update update);
}
