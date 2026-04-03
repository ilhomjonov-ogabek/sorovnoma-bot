package spring.boot.sorovnomabot.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import spring.boot.sorovnomabot.enums.RequestEnum;
import spring.boot.sorovnomabot.service.BotRequestService;

@Service
@RequiredArgsConstructor
public class AllMethodsService {

  private final BotRequestService botRequestService;

  public RequestEnum   requestCheckReturnEnum(Update update) {

    if (botRequestService.equalsStart(update)) {
      return RequestEnum.START;
    } else if (botRequestService.equalsPhoto(update)) {
      return RequestEnum.DEFAULT;
    } else if (botRequestService.isInPollCreation(update)) {
      return RequestEnum.POLL_CREATION;
    } else if (botRequestService.pressSimpleStart(update)) {
      return RequestEnum.SIMPLE_START;
    } else if (botRequestService.equalsSendPolls(update)) {
      return RequestEnum.SEND_POLLS;
    } else if (botRequestService.pickPoll(update)) {
      return RequestEnum.PICK_POLL;
    } else if (botRequestService.pressGetResult(update)) {
      return RequestEnum.GET_RESULT;
    } else if (botRequestService.pressInformation(update)) {
      return RequestEnum.INFORMATION;
    } else if (botRequestService.equalsVote(update)) {
      return RequestEnum.VOTE;
    } else if (botRequestService.equalsAdminMenu(update)) {
      return RequestEnum.ADMIN;
    } else if (botRequestService.equalsActivePolls(update)) {
      return RequestEnum.ACTIVE_POLLS;
    } else if (botRequestService.pressPoll(update)) {
      return RequestEnum.POLL;
    } else if (botRequestService.pressFinishingPoll(update)) {
      return RequestEnum.FINISHING_POLL;
    } else if (botRequestService.pressFinishPoll(update)) {
      return RequestEnum.FINISH_POLL;
    } else if (botRequestService.pressGetResultExcel(update)) {
      return RequestEnum.GET_RESULT_EXCEL;
    }  else if (botRequestService.pressPassivePolls(update)) {
      return RequestEnum.PASSIVE_POLLS;
    } else if (botRequestService.pressExportPoll(update)) {
      return RequestEnum.EXPORT;
    } else if (botRequestService.pressClearVotes(update)) {
      return RequestEnum.PICK_CLEAR_VOTES;
    } else if (botRequestService.pressAddAdmin(update)) {
      return RequestEnum.ADD_ADMIN;
    } else if (botRequestService.equalsClearVotes(update)) {
      return RequestEnum.CLEAR_VOTES;
    } else if (botRequestService.equalsNewAdmin(update)) {
      return RequestEnum.NEW_ADMIN;
    } else if (botRequestService.pressRemoveAdmin(update)) {
      return RequestEnum.REMOVE_ADMIN;
    } else if (botRequestService.pressApprove(update)) {
      return RequestEnum.APPROVE_POLL;
    } else if (botRequestService.pressNotApproved(update)) {
      return RequestEnum.NOT_APPROVED_POLL;
    } else if (botRequestService.pressRemovedAdmin(update)) {
      return RequestEnum.REMOVED_ADMIN;
    } else if (botRequestService.pressSubscribe(update)) {
      return RequestEnum.SUBSCRIBE;
    } else if (botRequestService.pressCreatePoll(update)) {
      return RequestEnum.CREATE_POLL;
    }
    return RequestEnum.DEFAULT;

  }
}
