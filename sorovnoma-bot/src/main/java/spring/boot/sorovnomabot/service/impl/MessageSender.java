package spring.boot.sorovnomabot.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import spring.boot.sorovnomabot.config.TelegramBotConfig;
import spring.boot.sorovnomabot.entity.Candidate;
import spring.boot.sorovnomabot.entity.Poll;
import spring.boot.sorovnomabot.repository.CandidateRepository;

@Service
@Log4j2
public class MessageSender {

  private final TelegramBotConfig bot;
  private final CandidateRepository candidateRepository;
  private final ObjectMapper objectMapper;

  public MessageSender(TelegramBotConfig bot, CandidateRepository candidateRepository,
      ObjectMapper objectMapper) {
    this.bot = bot;
    this.candidateRepository = candidateRepository;
    this.objectMapper = objectMapper;
  }


  public Message send(SendMessage sendMessage) {
    try {
      return bot.execute(sendMessage);
    } catch (TelegramApiException e) {
      throw new RuntimeException("Xabar yuborishda xatolik: " + e.getMessage());
    }
  }


  public void send(EditMessageCaption message) {
    try {
      bot.execute(message);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }


  public <T extends Serializable> T execute(BotApiMethod<T> method) {
    try {
      return bot.execute(method);
    } catch (TelegramApiException e) {
      e.printStackTrace();
      return null;
    }
  }

  public Chat executeChat(GetChat getChat) {
    try {
      return bot.execute(getChat);
    } catch (TelegramApiException e) {
      throw new RuntimeException(e);
    }
  }



  public Message sendPollToAprove(Poll p, Long chatId) {
    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
    for (Long candidateId : p.getCandidatesId()) {
      Candidate candidate = candidateRepository.findById(candidateId).orElseThrow();

      int voteCount = candidate.getVoteCount();
      String formatted = String.valueOf(voteCount);
      if (voteCount > 999 && voteCount  < 1000000) {
        formatted = String.format("%.1fk", (double) voteCount / 1000);
      } else if (voteCount > 1000000) {
        formatted = String.format("%.1fM", (double) voteCount / 1000000);
      }

      InlineKeyboardButton button = new InlineKeyboardButton();
      button.setText(candidate.getName() + " - " + formatted);
      button.setCallbackData("notApproved");
      rows.add(List.of(button));
    }

    InlineKeyboardButton approveButton = new InlineKeyboardButton();
    approveButton.setText("Kanalga yuborish");
    approveButton.setCallbackData("approve"+ "#" + p.getId());
    rows.add(List.of(approveButton));

    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
    markup.setKeyboard(rows);

    String caption = p.getTitle(); List<MessageEntity> entities = List.of();
    try {
      entities = objectMapper.readValue(
          p.getTitleEntities(),
          new TypeReference<>() {
          }
      );
    } catch (JsonProcessingException e) {
      log.error(e.getMessage());
    }


    SendPhoto sendPhoto = new SendPhoto();
    sendPhoto.setChatId(chatId.toString());
    sendPhoto.setPhoto(new InputFile(p.getPictureId()));
    sendPhoto.setCaption(caption);
    sendPhoto.setCaptionEntities(entities);
    sendPhoto.setReplyMarkup(markup);

    try {
      return bot.execute(sendPhoto);
    } catch (TelegramApiException e) {
      throw new RuntimeException(e);
    }
  }

  public Message sendPollToChannel(SendPhoto sendMessage) {
    try {
      return bot.execute(sendMessage);
    } catch (TelegramApiException e) {
      throw new RuntimeException(e);
    }
  }

  public void deleteMessage(DeleteMessage deleteMessage) {
    execute(deleteMessage);
  }

  public Long getBotId() {
    try {
      return bot.getMe().getId();
    } catch (TelegramApiException e) {
      throw new RuntimeException(e);
    }
  }

  public Message sendPoll(SendPhoto sendPhoto) {
    try {
      return bot.execute(sendPhoto);
    } catch (TelegramApiException e) {
      throw new RuntimeException(e);
    }
  }
}