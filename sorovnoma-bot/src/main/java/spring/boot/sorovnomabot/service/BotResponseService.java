package spring.boot.sorovnomabot.service;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface BotResponseService {

  SendPhoto pressStart(Update update);

  BotApiMethod<?> pressVote(Update update);

  BotApiMethod<?> pressAdminPage(Update update);

  BotApiMethod<?> pressActivePolls(Update update);

  BotApiMethod<?> pressPoll(Update update);

  BotApiMethod<?> pressFinishPoll(Update update);

  SendDocument pressGetResultExcel(Update update);

  BotApiMethod<?> pressCreatePoll(Update update);

  BotApiMethod<?> handlePollCreation(Update update);

  BotApiMethod<?> pressPassivePolls(Update update);

  BotApiMethod<?> pressSimpleStart(Update update);

  SendPhoto pressPickPoll(Update update);

  BotApiMethod<?> pressInformation(Update update);

  BotApiMethod<?> pressGetResult(Update update);

  BotApiMethod<?> pressFinishingPoll(Update update);

  BotApiMethod<?> pressExportPoll(Update update);

  BotApiMethod<?> pressPickClearVotes(Update update);

  BotApiMethod<?> pressClearVotes(Update update);

  BotApiMethod<?> pressAddAdmin(Update update);

  BotApiMethod<?> newAdmin(Update update);

  BotApiMethod<?> pressRemoveAdmin(Update update);

  BotApiMethod<?> removedAdmin(Update update);

  BotApiMethod<?> defaultMessage(Update update);

  SendMessage approvePoll(Update update);

  BotApiMethod<?> pressSubscribe(Update update);
}
