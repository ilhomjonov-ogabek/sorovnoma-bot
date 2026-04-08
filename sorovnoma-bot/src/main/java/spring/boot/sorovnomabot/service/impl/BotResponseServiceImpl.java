package spring.boot.sorovnomabot.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import spring.boot.sorovnomabot.entity.Candidate;
import spring.boot.sorovnomabot.entity.Poll;
import spring.boot.sorovnomabot.entity.User;
import spring.boot.sorovnomabot.enums.PollCreateState;
import spring.boot.sorovnomabot.enums.UserRole;
import spring.boot.sorovnomabot.repository.CandidateRepository;
import spring.boot.sorovnomabot.repository.PollRepository;
import spring.boot.sorovnomabot.repository.UserRepository;
import spring.boot.sorovnomabot.service.BotResponseService;

@Service
@Slf4j
public class BotResponseServiceImpl implements BotResponseService {

  private final UserRepository userRepository;
  private final PollRepository pollRepository;
  private final CandidateRepository candidateRepository;
  private final MessageSender messageSender;
  private final PollCreationStateHolder stateHolder;
  private final ObjectMapper objectMapper;

  private final Map<Long, Poll> pollDrafts = new ConcurrentHashMap<>();
  private final Map<Long, List<String>> candidateDrafts = new ConcurrentHashMap<>();
  private final Map<Long, List<String>> channelDrafts = new ConcurrentHashMap<>();
  private final Map<Long, LocalDateTime> voteRateLimit = new ConcurrentHashMap<>();
  private final Map<Long, Boolean> addAdminStatus = new ConcurrentHashMap<>();
  private final Map<Long, Boolean> removeAdminStatus = new ConcurrentHashMap<>();
  private final Map<Long, Boolean> createPollStatus = new ConcurrentHashMap<>();
  private final Map<Long, String> titleEntities = new ConcurrentHashMap<>();


  @Value("${telegram.channel.id}")
  private String channelId;

  @Value("${bot.username.new}")
  private String botUsername;

  public BotResponseServiceImpl(UserRepository userRepository, PollRepository pollRepository,
      CandidateRepository candidateRepository, @Lazy MessageSender messageSender,
      PollCreationStateHolder stateHolder, ObjectMapper objectMapper) {
    this.userRepository = userRepository;
    this.pollRepository = pollRepository;
    this.candidateRepository = candidateRepository;
    this.messageSender = messageSender;
    this.stateHolder = stateHolder;
    this.objectMapper = objectMapper;
  }

  @Override
  public void pressStart(Update update) {
    Long chatId = update.getMessage().getChatId();
    log.info("pressStart chaqirildi: chatId={}", chatId);

    Optional<User> user = userRepository.findByChatId(chatId);
    if (user.isEmpty()) {
      User newUser = User.builder()
          .chatId(chatId)
          .firstName(update.getMessage().getFrom().getFirstName())
          .lastName(update.getMessage().getFrom().getLastName())
          .username(Optional.ofNullable(update.getMessage().getFrom().getUserName())
              .map(String::toLowerCase).orElse(null))
          .role(UserRole.USER)
          .build();
      userRepository.save(newUser);
      log.info("Yangi user saqlandi: chatId={}, username={}", chatId, newUser.getUsername());
    }

    String pollId = update.getMessage().getText().substring(6).trim();
    log.debug("Deep link pollId: {}", pollId);

    Optional<Poll> poll = pollRepository.findById(Long.parseLong(pollId));

    if (poll.isEmpty()) {
      log.warn("So'rovnoma topilmadi: pollId={}", pollId);
      messageSender.send(sendText(chatId, "❌Bunday so'rovnoma mavjud emas!!!!!"));
    } else if (!poll.get().isActive()) {
      log.info("Yakunlangan so'rovnoma ochilmoqda: pollId={}", pollId);
      messageSender.send(sendText(chatId, "❌Bu so'rovnoma allaqachon yakunlangan!!!!!"));
    } else {

      messageSender.sendPoll(sendPoll(chatId, poll));
    }
  }

  @Override
  public BotApiMethod<?> pressVote(Update update) {
    Long chatId = update.getCallbackQuery().getMessage().getChatId();
    Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
    Optional<User> user = userRepository.findByChatId(chatId);
    String[] split = update.getCallbackQuery().getData().split("#");
    String candidateId = split[1];
    String pollId = split[2];

    if (isRateLimited(chatId)) {
      log.warn("Rate limit: chatId={}", chatId);
      return sendText(chatId, "⏳ Iltimos biroz kuting...");
    }

    log.info("pressVote: chatId={}, pollId={}, candidateId={}", chatId, pollId, candidateId);

    Optional<Poll> poll = pollRepository.findById(Long.parseLong(pollId));
    List<Long> usersId = poll.get().getUsersId();

    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(chatId.toString());
    List<String> channellsId = new ArrayList<>(poll.get().getChannellsId());
    channellsId.add(channelId);

    List<List<InlineKeyboardButton>> rows = new ArrayList<>();

    for (String channels : channellsId) {
      if (!isSubscribed(chatId, channels)) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("📢 Kanalga obuna bo'lish");
        button.setUrl("https://t.me/" + channels.replace("@", ""));
        rows.add(List.of(button));
      }
    }

    if (!rows.isEmpty()) {
      InlineKeyboardButton button2 = new InlineKeyboardButton();
      button2.setText("Obuna bo'ldim✅");
      button2.setCallbackData("subscribe" + "#" + pollId);
      log.info("User kanalga obuna emas: chatId={}", chatId);
      sendMessage.setText("❌ Ovoz berish uchun kanalga obuna bo'ling 👇");
      rows.add(List.of(button2));
      InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
      markup.setKeyboard(rows);
      sendMessage.setReplyMarkup(markup);
      return sendMessage;
    }

    if (poll.get().getFinishedDate().isBefore(LocalDate.now()) || !poll.get().isActive()) {
      log.info("Yakunlangan so'rovnomaga ovoz berishga urinish: pollId={}, chatId={}", pollId,
          chatId);
      sendMessage.setText("❌Bu so'rovnoma allaqachon yakunlangan!!" + "\n" +
          "Tugallangan sana:" + poll.get().getFinishedDate());
      return sendMessage;
    }

    if (poll.get().getStartDate().isAfter(LocalDate.now())) {
      log.info("Boshlanmagan so'rovnomaga ovoz berishga urinish: pollId={}, chatId={}", pollId,
          chatId);
      sendMessage.setText("❌Bu so'rovnoma boshlanmagan!!" + "\n"
          + "Boshlanish sanasi:" + poll.get().getStartDate());
      return sendMessage;
    }

    if (usersId.contains(user.get().getId())) {
      log.info("Takroriy ovoz berish urinishi: chatId={}, pollId={}", chatId, pollId);
      sendMessage.setText("🗿Siz allaqachon ovoz bergansiz");
      return sendMessage;
    }

    candidateRepository.incrementVoteCount(Long.parseLong(candidateId));
    usersId.add(user.get().getId());
    pollRepository.save(poll.get());

    log.info("Ovoz berildi: chatId={}, pollId={}, candidateId={}, jami ovoz={}", chatId, pollId,
        candidateId, usersId.size());

    return editPoll(chatId, poll, messageId);
  }

  @Override
  public BotApiMethod<?> pressAdminPage(Update update) {
    Long chatId = update.getMessage().getChatId();
    log.info("pressAdminPage: chatId={}", chatId);

    Optional<User> byChatId = userRepository.findByChatId(chatId);
    User user = byChatId.get();

    SendMessage sendMessage = new SendMessage();
    sendMessage.setChatId(chatId.toString());

    if (!(user.getRole() == UserRole.ADMIN || user.getRole() == UserRole.SUPER_ADMIN)) {
      log.warn("Ruxsatsiz admin panel urinishi: chatId={}, role={}", chatId, user.getRole());
      sendMessage.setText("Nomalum so'rov!");
      return sendMessage;
    }

    ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
    keyboard.setResizeKeyboard(true);

    KeyboardRow row1 = new KeyboardRow();
    row1.add(new KeyboardButton("➕Yangi so'rovnoma"));
    row1.add(new KeyboardButton("\uD83D\uDCCA Natijalar"));

    KeyboardRow row2 = new KeyboardRow();
    row2.add(new KeyboardButton("\uD83D\uDCCB Batafsil hisobot"));
    row2.add(new KeyboardButton("\uD83D\uDCE5 Eksport"));

    KeyboardRow row3 = new KeyboardRow();
    row3.add(new KeyboardButton("⏸\uFE0F Konkursni to'xtatish"));
    row3.add(new KeyboardButton("\uD83D\uDCDA Arxiv"));

    KeyboardRow row4 = new KeyboardRow();
    row4.add(new KeyboardButton("\uD83D\uDDD1 Ovozlarni tozalash"));

    keyboard.setKeyboard(new ArrayList<>(List.of(row1, row2, row3, row4)));

    if (user.getRole() == UserRole.SUPER_ADMIN) {
      KeyboardRow rowAdmin = new KeyboardRow();
      rowAdmin.add(new KeyboardButton("Admin qo'shish➕"));
      rowAdmin.add(new KeyboardButton("Admin o'chirish⛔"));
      keyboard.getKeyboard().add(rowAdmin);
    }

    KeyboardRow back = new KeyboardRow();
    back.add(new KeyboardButton("🔚 Orqaga"));
    keyboard.getKeyboard().add(back);

    sendMessage.setText("🥳Admin panelga xush kelibsiz!");
    sendMessage.setReplyMarkup(keyboard);
    return sendMessage;
  }

  @Override
  public BotApiMethod<?> pressActivePolls(Update update) {
    return pressActivePolls(update, "pick_poll");
  }

  @Override
  public BotApiMethod<?> pressPoll(Update update) {
    String pollId = update.getCallbackQuery().getData().substring(5);
    Long chatId = update.getCallbackQuery().getMessage().getChatId();
    Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

    log.info("pressPoll: chatId={}, pollId={}", chatId, pollId);

    Optional<Poll> byId = pollRepository.findById(Long.parseLong(pollId));
    Poll poll = byId.get();

    EditMessageText editMessageText = new EditMessageText();

    StringBuilder text = new StringBuilder();
    text.append("📊 ").append(poll.getTitle().toUpperCase()).append("\n\n");
    text.append("👥 Jami ovoz berganlar: ").append(poll.getUsersId().size()).append("\n\n");
    text.append("🏆 Natijalar:\n");

    for (Long candidateId : poll.getCandidatesId()) {
      Candidate candidate = candidateRepository.findById(candidateId).orElseThrow();
      text.append("• ").append(candidate.getName())
          .append(" — ").append(candidate.getVoteCount()).append(" ovoz\n");
    }

    text.append("\n📅 Tugash sanasi: ").append(poll.getFinishedDate());

    editMessageText.setText(text.toString());
    editMessageText.setChatId(chatId.toString());
    editMessageText.setMessageId(messageId);

    return editMessageText;
  }

  @Override
  public BotApiMethod<?> pressFinishPoll(Update update) {
    String pollId = update.getCallbackQuery().getData().substring(11);
    Long chatId = update.getCallbackQuery().getMessage().getChatId();
    Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

    log.info("pressFinishPoll: chatId={}, pollId={}", chatId, pollId);

    Poll poll = pollRepository.findById(Long.parseLong(pollId)).orElseThrow();
    poll.setActive(false);
    poll.setFinishedDate(LocalDate.now());
    pollRepository.save(poll);

    log.info("So'rovnoma yakunlandi: pollId={}", pollId);

    EditMessageText editMessageText = new EditMessageText();
    editMessageText.setText("So'rovnoma muvaffaqiyatli yakunlandi✅");
    editMessageText.setChatId(chatId.toString());
    editMessageText.setMessageId(messageId);
    return editMessageText;
  }

  @Override
  public SendDocument pressGetResultExcel(Update update) {
    Long chatId = update.getCallbackQuery().getMessage().getChatId();
    String pollId = update.getCallbackQuery().getData().substring(10);

    log.info("pressGetResultExcel: chatId={}, pollId={}", chatId, pollId);

    Poll poll = pollRepository.findById(Long.parseLong(pollId)).orElseThrow();

    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Natijalar");

    CellStyle headerStyle = workbook.createCellStyle();
    Font font = workbook.createFont();
    font.setBold(true);
    headerStyle.setFont(font);

    Row header = sheet.createRow(0);
    String[] headers = {"Ism", "Ovoz soni", "Foiz (%)"};
    for (int i = 0; i < headers.length; i++) {
      Cell cell = header.createCell(i);
      cell.setCellValue(headers[i]);
      cell.setCellStyle(headerStyle);
    }

    int totalVotes = poll.getUsersId().size();
    int rowNum = 1;
    for (Long candidateId : poll.getCandidatesId()) {
      Candidate candidate = candidateRepository.findById(candidateId).orElseThrow();
      Row row = sheet.createRow(rowNum++);
      row.createCell(0).setCellValue(candidate.getName());
      row.createCell(1).setCellValue(candidate.getVoteCount());
      double percent = totalVotes == 0 ? 0 : (candidate.getVoteCount() * 100.0 / totalVotes);
      row.createCell(2).setCellValue(Math.round(percent * 10.0) / 10.0);
    }

    Row totalRow = sheet.createRow(rowNum);
    totalRow.createCell(0).setCellValue("JAMI");
    totalRow.createCell(1).setCellValue(totalVotes);
    totalRow.createCell(2).setCellValue(100.0);

    for (int i = 0; i < 3; i++) {
      sheet.autoSizeColumn(i);
    }

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      workbook.write(out);
      workbook.close();
    } catch (IOException e) {
      log.error("Excel yaratishda xatolik: pollId={}", pollId, e);
      throw new RuntimeException(e);
    }

    log.info("Excel muvaffaqiyatli yaratildi: pollId={}, jami ovoz={}", pollId, totalVotes);

    SendDocument sendDocument = new SendDocument();
    sendDocument.setChatId(chatId.toString());
    sendDocument.setCaption("📊 " + poll.getTitle() + " — natijalar");
    sendDocument.setDocument(new InputFile(
        new ByteArrayInputStream(out.toByteArray()),
        "natijalar.xlsx"
    ));

    return sendDocument;
  }

  @Override
  public BotApiMethod<?> pressCreatePoll(Update update) {
    Long chatId = update.getMessage().getChatId();
    log.info("pressCreatePoll: chatId={}", chatId);

    if (userRepository.findByChatId(chatId).get().getRole().equals(UserRole.USER)) {
      log.warn("Ruxsatsiz so'rovnoma yaratish urinishi: chatId={}", chatId);
      return sendText(chatId, "Bu so'rov uchun ruxsat yo'q⛔");
    }
    stateHolder.put(chatId, PollCreateState.WAITING_PICTURE);
    pollDrafts.put(chatId, new Poll());
    candidateDrafts.put(chatId, new ArrayList<>());
    channelDrafts.put(chatId, new ArrayList<>());
    createPollStatus.put(chatId, true);

    return sendText(chatId, "📝 So'rovnoma rasmini kiriting:" + "\n"
        + "So'rovnoma yaratishni bekor qilish uchun: /exit");
  }

  private BotApiMethod<?> editPoll(Long chatId, Optional<Poll> poll, Integer messageId) {
    Poll p = poll.get();

    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
    for (Long candidateId : p.getCandidatesId()) {
      Candidate candidate = candidateRepository.findById(candidateId).orElseThrow();

      int voteCount = candidate.getVoteCount();
      String formatted = String.valueOf(voteCount);
      if (voteCount > 999 && voteCount < 1000000) {
        formatted = String.format("%.1fk", (double) voteCount / 1000);
      } else if (voteCount > 1000000) {
        formatted = String.format("%.1fM", (double) voteCount / 1000000);
      }

      InlineKeyboardButton button = new InlineKeyboardButton();
      button.setText(candidate.getName() + " - " + formatted);
      button.setCallbackData("vote" + "#" + candidateId + "#" + p.getId());

      rows.add(List.of(button));
    }

    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
    markup.setKeyboard(rows);

    String caption = p.getTitle();
    List<MessageEntity> entities = List.of();
    try {
      entities = objectMapper.readValue(
          poll.get().getTitleEntities(),
          new TypeReference<>() {
          }
      );
    } catch (JsonProcessingException e) {
      log.error(e.getMessage());
    }

    EditMessageCaption message = new EditMessageCaption();
    message.setChatId(chatId.toString());
    message.setMessageId(messageId);
    message.setCaption(caption);
    message.setCaptionEntities(entities);
    message.setReplyMarkup(markup);

    sendPollToChannelEditMessage(poll.get());

    return message;
  }

  private SendPhoto sendPoll(Long chatId, Optional<Poll> poll) {
    Poll p = poll.get();

    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
    for (Long candidateId : p.getCandidatesId()) {
      Candidate candidate = candidateRepository.findById(candidateId).orElseThrow();

      int voteCount = candidate.getVoteCount();
      String formatted = String.valueOf(voteCount);
      if (voteCount > 999 && voteCount < 1000000) {
        formatted = String.format("%.1fk", (double) voteCount / 1000);
      } else if (voteCount > 1000000) {
        formatted = String.format("%.1fM", (double) voteCount / 1000000);
      }

      InlineKeyboardButton button = new InlineKeyboardButton();
      button.setText(candidate.getName() + " - " + formatted);
      button.setCallbackData("vote" + "#" + candidateId + "#" + p.getId());
      rows.add(List.of(button));
    }

    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
    markup.setKeyboard(rows);

    String caption = p.getTitle();
    List<MessageEntity> entities = List.of();
    try {
      entities = objectMapper.readValue(
          poll.get().getTitleEntities(),
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

    return sendPhoto;
  }

  @Override
  public BotApiMethod<?> handlePollCreation(Update update) {
    Long chatId = update.getMessage().getChatId();

    if (!createPollStatus.getOrDefault(chatId, false)) {
      return defaultMessage(update);
    }

    if (userRepository.findByChatId(chatId).get().getRole().equals(UserRole.USER)) {
      log.warn("Ruxsatsiz so'rovnoma yaratish urinishi: chatId={}", chatId);
      return sendText(chatId, "Bu so'rov uchun ruxsat yo'q⛔");
    }
    String text;
    if (!update.getMessage().hasPhoto()) {
      text = update.getMessage().getText();
    } else {
      List<PhotoSize> photos = update.getMessage().getPhoto();
      PhotoSize photo = photos.get(photos.size() - 1);
      text = photo.getFileId();
    }
    PollCreateState state = stateHolder.get(chatId);
    Poll draft = pollDrafts.get(chatId);

    log.info("handlePollCreation: chatId={}, state={}, text={}", chatId, state, text);

    switch (state) {
      case WAITING_PICTURE -> {
        if (!update.getMessage().hasPhoto() && update.getMessage().getText().equals("/exit")) {
          return pressExit(update);
        }
        if (!update.getMessage().hasPhoto()) {
          return pressCreatePoll(update);
        }
        draft.setPictureId(text);
        stateHolder.put(chatId, PollCreateState.WAITING_TITLE);
        return sendText(chatId, "📝 So'rovnoma sarlavhasini kiriting:" + "\n"
            + "So'rovnoma yaratishni bekor qilish uchun: /exit");
      }
      case WAITING_TITLE -> {
        if (!update.getMessage().hasText()) {
          return sendText(chatId, "Xatolik iltimos text formatda ma'lumot kiriting!" + "\n"
              + "📝 So'rovnoma sarlavhasini kiriting:" + "\n"
              + "So'rovnoma yaratishni bekor qilish uchun: /exit");
        }

        if (update.getMessage().getText().equals("/exit")) {
          return pressExit(update);
        }
        try {
          titleEntities.put(chatId,
              objectMapper.writeValueAsString(update.getMessage().getEntities()));
        } catch (JsonProcessingException e) {
          log.error("Error put title entity:" + e.getMessage());
        }
        draft.setTitle(text);
        stateHolder.put(chatId, PollCreateState.WAITING_START_DATE);
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return sendText(chatId,
            "📅 Boshlanish sanasini kiriting (yyyy-MM-dd):\n\nMasalan: " + "'" + today + "'"
                + "\n"
                + "So'rovnoma yaratishni bekor qilish uchun: /exit");
      }
      case WAITING_START_DATE -> {
        if (!update.getMessage().hasText()) {
          String text1 = "Xatolik iltimos sanani to'g'ri kiriting!" + "\n"
              + "Bugungi sana:" + "'" + LocalDate.now() + "'" + "\n"
              + "So'rovnoma yaratishni bekor qilish uchun: /exit";
          return sendText(chatId, text1);
        }
        if (update.getMessage().getText().equals("/exit")) {
          return pressExit(update);
        }
        try {
          if (LocalDate.parse(text).isBefore(LocalDate.now())) {
            String text1 = "Xatolik kiritilgan sana bugungi sanadan katta bo'lishi kerak!" + "\n"
                + "Bugungi sana:" + "'" + LocalDate.now() + "'" + "\n"
                + "So'rovnoma yaratishni bekor qilish uchun: /exit";
            return sendText(chatId, text1);
          }
          draft.setStartDate(LocalDate.parse(text));
          stateHolder.put(chatId, PollCreateState.WAITING_FINISHED_DATE);
          String startDate = draft.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
          return sendText(chatId,
              "📅 Tugash sanasini kiriting (yyyy-MM-dd):\n\nMasalan:" + "'" + startDate + "'" + "\n"
                  + "So'rovnoma yaratishni bekor qilish uchun: /exit");
        } catch (Exception e) {
          return sendText(chatId, "❌ Noto'g'ri format! Qaytadan kiriting (yyyy-MM-dd):" + "\n"
              + "So'rovnoma yaratishni bekor qilish uchun: /exit");
        }
      }
      case WAITING_FINISHED_DATE -> {
        if (!update.getMessage().hasText()) {
          String text1 = "Xatolik iltimos sanani to'g'ri kiriting!" + "\n"
              + "Bugungi sana:" + "'" + LocalDate.now() + "'" + "\n"
              + "So'rovnoma yaratishni bekor qilish uchun: /exit";
          return sendText(chatId, text1);
        }
        if (update.getMessage().getText().equals("/exit")) {
          return pressExit(update);
        }
        try {
          if (LocalDate.parse(text).isBefore(draft.getStartDate())) {
            String text1 =
                "Xatolik kiritilgan sana boshlanish sanasidan katta bo'lishi kerak!" + "\n"
                    + "Boshlanish sanasi:" + draft.getStartDate() + "\n"
                    + "So'rovnoma yaratishni bekor qilish uchun: /exit";
            return sendText(chatId, text1);
          }
          draft.setFinishedDate(LocalDate.parse(text));
          stateHolder.put(chatId, PollCreateState.WAITING_CHANNELS);
          return sendText(chatId, "Kanal linkini kiriting kiriting.\nTugagach /done yozing:" + "\n"
              + "So'rovnoma yaratishni bekor qilish uchun: /exit");
        } catch (Exception e) {
          return sendText(chatId, "❌ Noto'g'ri format! Qaytadan kiriting (yyyy-MM-dd):" + "\n"
              + "So'rovnoma yaratishni bekor qilish uchun: /exit");
        }
      }
      case WAITING_CHANNELS -> {
        if (!update.getMessage().hasText()) {
          return sendText(chatId, "Xatolik iltimos text formatda ma'lumot kiriting!" + "\n"
              + "➕ Kanal linkini kiriting.\nTugagach yoki o'tkazib yuborish uchun: /done" + "\n"
              + "So'rovnoma yaratishni bekor qilish uchun: /exit");
        }
        if (update.getMessage().getText().equals("/exit")) {
          return pressExit(update);
        }
        if (text.equals("/done")) {
          if (channelDrafts.get(chatId).isEmpty()) {
            log.info("Kanal linklari ulanmadi:{}", chatId);
            channelDrafts.put(chatId, new ArrayList<>());
            stateHolder.put(chatId, PollCreateState.WAITING_CANDIDATES);
            return sendText(chatId, "✅Obuna uchun kanal ulanmadi" + "\n" +
                "👤 Nomzod ismini kiritishingiz mumkin!" + "\n"
                + "So'rovnoma yaratishni bekor qilish uchun: /exit");
          }
          log.info("So'rovnoma uchun kanallar qo'shilmoqda: chatId={}", chatId);
          stateHolder.put(chatId, PollCreateState.WAITING_CANDIDATES);
          return sendText(chatId, "👤 Nomzod ismini kiriting.\nTugagach /done yozing:" + "\n"
              + "So'rovnoma yaratishni bekor qilish uchun: /exit");
        }
        if (!text.startsWith("@")) {
          return sendText(chatId,
              "❌Kiritilayotgan format xato iltimos kanal linki '@' bilan boshlanishi kerak!"
                  + "\n"
                  + "So'rovnoma yaratishni bekor qilish uchun: /exit");
        }
        if (!isChannelExists(text)) {
          return sendText(chatId, "Bunday linkdagi kanal topilmadi iltimos!" + "\n"
              + "So'rovnoma yaratishni bekor qilish uchun: /exit");
        }
        if (!isBotAdminInChannel(text)) {
          return sendText(chatId,
              "❌Iltimos tekshiring bot tashlangan kanalda admin bo'lishi kerak" + "\n"
                  + "So'rovnoma yaratishni bekor qilish uchun: /exit");
        }
        channelDrafts.get(chatId).add(text);
        log.info("Kanal qo'shildi: chatId={}, candidateName={}", chatId, text);
        return sendText(chatId, "✅ Qo'shildi! Yana kanal link yoki /done:" + "\n"
            + "So'rovnoma yaratishni bekor qilish uchun: /exit");
      }
      case WAITING_CANDIDATES -> {
        if (!update.getMessage().hasText()) {
          return sendText(chatId, "Xatolik iltimos text formatda ma'lumot kiriting!" + "\n"
              + "👤 Nomzod ismini kiriting.\nTugagach /done yozing:" + "\n"
              + "So'rovnoma yaratishni bekor qilish uchun: /exit");
        }
        if (update.getMessage().getText().equals("/exit")) {
          return pressExit(update);
        }
        if (text.equals("/done")) {
          if (candidateDrafts.get(chatId).size() < 2) {
            return sendText(chatId, "So'rovnomada kamida 2 kishi qatnashishi kerak!" + "\n"
                + "So'rovnoma yaratishni bekor qilish uchun: /exit");
          }
          log.info("So'rovnoma yaratish yakunlanmoqda: chatId={}", chatId);
          return finishPollCreation(chatId);
        }

        Candidate candidate = new Candidate();
        candidate.setName(text);
        candidate.setVoteCount(0);
        Candidate saved = candidateRepository.save(candidate);
        candidateDrafts.get(chatId).add(saved.getId().toString());
        log.info("Nomzod qo'shildi: chatId={}, candidateName={}", chatId, text);
        return sendText(chatId, "✅ Qo'shildi! Yana nomzod yoki /done:");
      }
    }
    return sendText(chatId, "Noma'lum holat");
  }

  @Override
  public BotApiMethod<?> pressPassivePolls(Update update) {
    Long chatId = update.getMessage().getChatId();
    if (userRepository.findByChatId(chatId).get().getRole().equals(UserRole.USER)) {
      log.warn("Ruxsatsiz arxiv ko'rish urinishi: chatId={}", chatId);
      return sendText(chatId, "Bu so'rov uchun ruxsat yo'q⛔");
    }
    return pressPassivePolls(update, "poll_");
  }

  public BotApiMethod<?> pressPassivePolls(Update update, String callbackData) {
    List<Poll> pollList = pollRepository.findByActiveAndChannelMessageIdIsNotNull(false);
    Long chatId = update.getMessage().getChatId();

    List<List<InlineKeyboardButton>> rows = new ArrayList<>();

    for (Poll poll : pollList) {
      InlineKeyboardButton button = new InlineKeyboardButton();
      button.setText(poll.getTitle());
      button.setCallbackData(callbackData + poll.getId());
      List<InlineKeyboardButton> row = new ArrayList<>();
      row.add(button);
      rows.add(row);
    }

    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
    markup.setKeyboard(rows);
    SendMessage sendMessage = new SendMessage();
    if (pollList.isEmpty()) {
      sendMessage.setText("Yakunlangan so'rovnomalar hali mavjud emas!");
    } else {
      sendMessage.setText("Yakunlangan so'rovnomalar🔚⛔");
    }
    sendMessage.setChatId(chatId.toString());
    sendMessage.setReplyMarkup(markup);

    return sendMessage;
  }

  @Override
  public BotApiMethod<?> pressSimpleStart(Update update) {
    Long chatId = update.getMessage().getChatId();
    log.info("pressSimpleStart: chatId={}", chatId);

    Optional<User> user = userRepository.findByChatId(chatId);
    if (user.isEmpty()) {
      User newUser = User.builder()
          .firstName(update.getMessage().getFrom().getFirstName())
          .lastName(update.getMessage().getFrom().getLastName())
          .username(Optional.ofNullable(update.getMessage().getFrom().getUserName())
              .map(String::toLowerCase).orElse(null))
          .role(UserRole.USER)
          .chatId(chatId)
          .build();
      userRepository.save(newUser);
      user = Optional.of(newUser);
      log.info("Yangi user saqlandi: chatId={}, username={}", chatId, newUser.getUsername());
    }

    ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
    keyboard.setResizeKeyboard(true);
    keyboard.setOneTimeKeyboard(false);

    KeyboardRow row = new KeyboardRow();
    row.add(new KeyboardButton("🗳️ Ovoz berish"));

    KeyboardRow row2 = new KeyboardRow();
    row2.add(new KeyboardButton("📊 Natijalar"));
    row2.add(new KeyboardButton("ℹ️ Ma'lumot"));

    List<KeyboardRow> rows = new ArrayList<>();
    rows.add(row);
    rows.add(row2);

    if (user.get().getRole().equals(UserRole.ADMIN) || user.get().getRole()
        .equals(UserRole.SUPER_ADMIN)) {
      KeyboardRow row3 = new KeyboardRow();
      row3.add(new KeyboardButton("👨🏻‍💻Admin Panel"));
      rows.add(row3);
    }

    keyboard.setKeyboard(rows);

    String text = "👋 Assalomu alaykum, " + user.get().getFirstName() + "!\n\n" +
        "📊 So'rovnoma botiga xush kelibsiz!\n\n" +
        "Bu bot orqali siz:\n" +
        "✅ Faol so'rovnomalarni ko'rishingiz\n" +
        "🗳 Ovoz berishingiz\n" +
        "📈 Natijalarni kuzatishingiz mumkin\n\n" +
        "👇 Boshlash uchun tugmani tanlang:";

    SendMessage sendMessage = sendText(chatId, text);
    sendMessage.setReplyMarkup(keyboard);
    return sendMessage;
  }

  @Override
  public SendPhoto pressPickPoll(Update update) {
    Long chatId = update.getCallbackQuery().getMessage().getChatId();
    String pollId = update.getCallbackQuery().getData().substring(9);
    log.info("pressPickPoll: chatId={}, pollId={}", chatId, pollId);
    Optional<Poll> byId = pollRepository.findById(Long.parseLong(pollId));
    return sendPoll(chatId, byId);
  }

  @Override
  public BotApiMethod<?> pressInformation(Update update) {
    Long chatId = update.getMessage().getChatId();
    log.info("pressInformation: chatId={}", chatId);

    String text = "👋 Assalomu alaykum!" + "\n\n" +
        "📊 So'rovnoma botiga xush kelibsiz!\n\n" +
        "Bu bot orqali siz:\n" +
        "✅ Faol so'rovnomalarni ko'rishingiz\n" +
        "🗳 Ovoz berishingiz\n" +
        "📈 Natijalarni kuzatishingiz mumkin\n\n" +
        "👇 Boshlash uchun ' \uD83D\uDDF3\uFE0F Ovoz berish ' tugmasini bosing:";

    return sendText(chatId, text);
  }

  @Override
  public BotApiMethod<?> pressGetResult(Update update) {
    Long chatId = update.getMessage().getChatId();
    log.info("pressGetResult: chatId={}", chatId);

    List<Poll> pollList = pollRepository.findByActiveAndChannelMessageIdIsNotNull(true);

    List<List<InlineKeyboardButton>> rows = new ArrayList<>();

    for (Poll poll : pollList) {
      InlineKeyboardButton button = new InlineKeyboardButton();
      button.setText(poll.getTitle());
      button.setCallbackData("poll_" + poll.getId());
      List<InlineKeyboardButton> row = new ArrayList<>();
      row.add(button);
      rows.add(row);
    }

    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
    markup.setKeyboard(rows);
    SendMessage sendMessage = new SendMessage();
    sendMessage.setText("Active so'rovnomalar✅🔜");
    sendMessage.setChatId(chatId.toString());
    sendMessage.setReplyMarkup(markup);

    return sendMessage;
  }

  @Override
  public BotApiMethod<?> pressFinishingPoll(Update update) {
    Long chatId = update.getMessage().getChatId();
    if (userRepository.findByChatId(chatId).get().getRole().equals(UserRole.USER)) {
      log.warn("Ruxsatsiz so'rovnomani to'xtatish urinishi: chatId={}", chatId);
      return sendText(chatId, "Bu so'rov uchun ruxsat yo'q⛔");
    }
    return pressActivePolls(update, "finish_poll");
  }

  @Override
  public BotApiMethod<?> pressExportPoll(Update update) {
    Long chatId = update.getMessage().getChatId();
    if (userRepository.findByChatId(chatId).get().getRole().equals(UserRole.USER)) {
      log.warn("Ruxsatsiz eksport urinishi: chatId={}", chatId);
      return sendText(chatId, "Bu so'rov uchun ruxsat yo'q⛔");
    }
    return pressPassivePolls(update, "get_result");
  }

  @Override
  public BotApiMethod<?> pressPickClearVotes(Update update) {
    Long chatId = update.getMessage().getChatId();
    if (userRepository.findByChatId(chatId).get().getRole().equals(UserRole.USER)) {
      log.warn("Ruxsatsiz ovoz tozalash urinishi: chatId={}", chatId);
      return sendText(chatId, "Bu so'rov uchun ruxsat yo'q⛔");
    }
    return pressActivePolls(update, "clear_votes");
  }

  @Override
  public BotApiMethod<?> pressClearVotes(Update update) {
    String pollId = update.getCallbackQuery().getData().substring(11);
    Long chatId = update.getCallbackQuery().getMessage().getChatId();
    Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

    log.info("pressClearVotes: chatId={}, pollId={}", chatId, pollId);

    Poll poll = pollRepository.findById(Long.parseLong(pollId)).orElse(null);
    if (poll == null) {
      log.warn("Poll topilmadi: pollId={}", pollId);
      return sendText(chatId, "❌ So'rovnoma topilmadi!");
    }
    List<Long> candidatesId = poll.getCandidatesId();
    candidateRepository.resetVoteCounts(candidatesId);
    poll.getUsersId().clear();
    pollRepository.save(poll);

    log.info("Ovozlar tozalandi: pollId={}", pollId);

    EditMessageText editMessage = new EditMessageText();
    editMessage.setChatId(chatId.toString());
    editMessage.setMessageId(messageId);
    editMessage.setText("Bu so'rovnomaning ovozlar soni tozalandi.");

    sendPollToChannelEditMessage(poll);

    return editMessage;
  }

  @Override
  public BotApiMethod<?> pressAddAdmin(Update update) {
    Long chatId = update.getMessage().getChatId();
    log.info("pressAddAdmin: chatId={}", chatId);

    Optional<User> user = userRepository.findByChatId(chatId);
    if (!user.get().getRole().equals(UserRole.SUPER_ADMIN)) {
      log.warn("Ruxsatsiz admin qo'shish urinishi: chatId={}", chatId);
      return sendText(chatId, "Bu so'rov uchun ruxsat yo'q⛔");
    }
    addAdminStatus.put(chatId, true);
    return sendText(chatId, "Yangi adminning usernameni to'g'ri formatda kiriting: " + "\n"
        + "Misol uchun: @username" + "\n"
        + "Eslatma! :admin botdan registratsiyadan o'tgan bo'lishi kerak!");
  }

  @Override
  public BotApiMethod<?> newAdmin(Update update) {
    Long chatId = update.getMessage().getChatId();
    Optional<User> user = userRepository.findByChatId(chatId);
    if (!user.get().getRole().equals(UserRole.SUPER_ADMIN)) {
      addAdminStatus.put(chatId, false);
      log.warn("Ruxsatsiz newAdmin urinishi: chatId={}", chatId);
      return sendText(chatId, "Bu so'rov uchun ruxsat yo'q⛔");
    }

    if (!addAdminStatus.getOrDefault(chatId, false)) {
      return sendText(chatId, "Noma'lum so'rov!");
    }

    String username = update.getMessage().getText().substring(1).toLowerCase();
    log.info("newAdmin username: {}", username);

    Optional<User> users = userRepository.getByUsername(username);
    if (users.isEmpty()) {
      addAdminStatus.put(chatId, false);
      log.warn("Admin qo'shish: user topilmadi, username={}", username);
      return sendText(chatId, "Bunday user topilmadi!");
    }
    if (users.get().getRole().equals(UserRole.ADMIN)) {
      addAdminStatus.put(chatId, false);
      log.info("Admin qo'shish: allaqachon admin, username={}", username);
      return sendText(chatId, "Bu user allaqachon adminlikka tayinlangan!");
    }
    users.get().setRole(UserRole.ADMIN);
    userRepository.save(users.get());
    addAdminStatus.put(chatId, false);

    log.info("Yangi admin tayinlandi: username={}", username);
    return sendText(chatId, "Yangi admin tayinlandi✅");
  }

  @Override
  public BotApiMethod<?> pressRemoveAdmin(Update update) {
    Long chatId = update.getMessage().getChatId();
    log.info("pressRemoveAdmin: chatId={}", chatId);

    Optional<User> user = userRepository.findByChatId(chatId);
    if (!user.get().getRole().equals(UserRole.SUPER_ADMIN)) {
      log.warn("Ruxsatsiz admin o'chirish urinishi: chatId={}", chatId);
      return sendText(chatId, "Bu so'rov uchun ruxsat yo'q⛔");
    }
    removeAdminStatus.put(chatId, true);
    List<User> admins = userRepository.getByRole(UserRole.ADMIN);
    if (admins.isEmpty()) {
      removeAdminStatus.put(chatId, false);
      return sendText(chatId, "Adminlar topilmadi!");
    }
    StringBuilder text = new StringBuilder();
    text.append("Adminlar:\n");
    for (User admin : admins) {
      text.append("@").append(admin.getUsername()).append("\n");
    }
    text.append("\nAdminning usernameni to'g'ri formatda kiriting: \n")
        .append("Misol uchun: remove@username\n")
        .append("Eslatma: username oldidan 'remove' so'zi qo'shilishi kerak.");
    return sendText(chatId, String.valueOf(text));
  }

  @Override
  public BotApiMethod<?> removedAdmin(Update update) {
    Long chatId = update.getMessage().getChatId();
    Optional<User> user = userRepository.findByChatId(chatId);
    if (!user.get().getRole().equals(UserRole.SUPER_ADMIN)) {
      removeAdminStatus.put(chatId, false);
      log.warn("Ruxsatsiz removedAdmin urinishi: chatId={}", chatId);
      return sendText(chatId, "Bu so'rov uchun ruxsat yo'q⛔");
    }

    if (!removeAdminStatus.getOrDefault(chatId, false)) {
      return sendText(chatId, "Noma'lum so'rov");
    }

    String username = update.getMessage().getText().toLowerCase().substring(7);
    log.info("removedAdmin username: {}", username);

    Optional<User> users = userRepository.getByUsernameAndRole(username, UserRole.ADMIN);
    if (users.isEmpty()) {
      log.warn("Admin o'chirish: admin topilmadi, username={}", username);
      return sendText(chatId, "Bunday admin topilmadi");
    }
    users.get().setRole(UserRole.USER);
    userRepository.save(users.get());
    removeAdminStatus.put(chatId, false);

    log.info("Admin o'chirildi: username={}", username);
    return sendText(chatId, "Admin o'chirildi✅");
  }

  @Override
  public BotApiMethod<?> defaultMessage(Update update) {
    Long chatId = update.getMessage().getChatId();
    log.info("defaultMessage: chatId={}, text={}", chatId, update.getMessage().getText());

    String text = "🤔 Bunday buyruq mavjud emas!\n\n" +
        "✅ Faqat tugmalar orqali amal bajaring\n" +
        "Yoki /start bosib boshidan boshlang";

    return sendText(chatId, text);
  }

  @Override
  public SendMessage approvePoll(Update update) {
    Long chatId = update.getCallbackQuery().getMessage().getChatId();
    Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
    String[] split = update.getCallbackQuery().getData().split("#");
    String pollId = split[1];
    Optional<Poll> byId = pollRepository.findById(Long.parseLong(pollId));
    sendPollToChannel(byId.get());

    DeleteMessage deleteMessage = new DeleteMessage();
    deleteMessage.setChatId(chatId.toString());
    deleteMessage.setMessageId(messageId);
    messageSender.deleteMessage(deleteMessage);

    return sendText(chatId, "So'rovnoma kanalga yuborildi✅");
  }

  @Override
  public BotApiMethod<?> pressSubscribe(Update update) {
    Long chatId = update.getCallbackQuery().getMessage().getChatId();
    Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
    String[] split = update.getCallbackQuery().getData().split("#");
    String pollId = split[1];
    Optional<Poll> poll = pollRepository.findById(Long.parseLong(pollId));
    List<String> channellsId = new ArrayList<>(poll.get().getChannellsId());
    channellsId.add(channelId);
    List<List<InlineKeyboardButton>> rows = new ArrayList<>();

    for (String channel : channellsId) {
      if (!isSubscribed(chatId, channel)) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("📢 Kanalga obuna bo'lish");
        button.setUrl("https://t.me/" + channel.replace("@", ""));
        rows.add(List.of(button));
      }
    }

    if (!rows.isEmpty()) {
      InlineKeyboardButton checkButton = new InlineKeyboardButton();
      checkButton.setText("Obuna bo'ldim✅");
      checkButton.setCallbackData("subscribe" + "#" + pollId);
      rows.add(List.of(checkButton));

      String newText = "❌ Ovoz berish uchun kanalga obuna bo'ling 👇";
      String oldText = update.getCallbackQuery().getMessage().getText();

      InlineKeyboardMarkup newMarkup = new InlineKeyboardMarkup();
      newMarkup.setKeyboard(rows);

      if (newText.equals(oldText)) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(update.getCallbackQuery().getId());
        answer.setText("⚠️ Iltimos, avval barcha kanallarga obuna bo'ling!");
        answer.setShowAlert(false);
        return answer;
      }

      log.info("User kanalga obuna emas: chatId={}", chatId);

      EditMessageText editMessage = new EditMessageText();
      editMessage.setChatId(chatId.toString());
      editMessage.setMessageId(messageId);
      editMessage.setText(newText);
      editMessage.setReplyMarkup(newMarkup);
      return editMessage;
    }

    EditMessageText editMessage = new EditMessageText();
    editMessage.setChatId(chatId.toString());
    editMessage.setMessageId(messageId);
    editMessage.setText("✅ Obunangiz tasdiqlandi, ovoz berishingiz mumkin");
    messageSender.sendPollToChannel(sendPoll(chatId, poll));

    return editMessage;
  }

  @Override
  public BotApiMethod<?> pressNotApprovedPoll(Update update) {
    Long chatId = update.getCallbackQuery().getMessage().getChatId();
    return sendText(chatId, "❌ Iltimos ovoz berishdan oldin kanalga yuborib tasqidlang!");
  }

  @Override
  public BotApiMethod<?> sendToAdminsStart(Long chatId, String s) {
    return sendText(chatId, s);
  }

  @Override
  public BotApiMethod<?> sendToAdminsStop(Long chatId, String s) {
    return sendText(chatId, s);
  }


  @Override
  public List<Long> getAdminsChatId() {
    return userRepository.getChatIdsByRoles(UserRole.ADMIN,
        UserRole.SUPER_ADMIN);
  }

  private SendMessage finishPollCreation(Long chatId) {
    Poll poll = pollDrafts.get(chatId);
    List<String> candidateIds = candidateDrafts.get(chatId);

    poll.setActive(true);
    poll.setCandidatesId(candidateIds.stream().map(Long::parseLong).toList());
    poll.setChannellsId(new ArrayList<>(channelDrafts.get(chatId)));
    poll.setUsersId(new ArrayList<>());
    poll.setTitleEntities(titleEntities.get(chatId));
    pollRepository.save(poll);

    stateHolder.remove(chatId);
    pollDrafts.remove(chatId);
    candidateDrafts.remove(chatId);
    channelDrafts.remove(chatId);
    titleEntities.remove(chatId);

    messageSender.sendPollToAprove(poll, chatId);
    createPollStatus.put(chatId, false);

    log.info("So'rovnoma muvaffaqiyatli yaratildi: pollId={}, title={}", poll.getId(),
        poll.getTitle());

    return sendText(chatId, "✅ So'rovnoma yaratildi kanalga yuborish uchun tasdiqlang!");
  }

  private void sendPollToChannel(Poll poll) {
    log.info("Kanalga so'rovnoma yuborilmoqda: pollId={}", poll.getId());

    List<List<InlineKeyboardButton>> rows = new ArrayList<>();

    for (Long candidateId : poll.getCandidatesId()) {
      Candidate candidate = candidateRepository.findById(candidateId).orElseThrow();

      int voteCount = candidate.getVoteCount();
      String formatted = String.valueOf(voteCount);
      if (voteCount > 999 && voteCount < 1000000) {
        formatted = String.format("%.1fk", (double) voteCount / 1000);
      } else if (voteCount > 1000000) {
        formatted = String.format("%.1fM", (double) voteCount / 1000000);
      }

      InlineKeyboardButton button = new InlineKeyboardButton();
      button.setText(candidate.getName() + " - " + formatted);
      button.setUrl("https://t.me/" + botUsername + "?start=" + poll.getId());

      rows.add(List.of(button));
    }

    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
    markup.setKeyboard(rows);

    String text = poll.getTitle();
    List<MessageEntity> entities = List.of();
    try {
      entities = objectMapper.readValue(
          poll.getTitleEntities(),
          new TypeReference<>() {
          }
      );
    } catch (JsonProcessingException e) {
      log.error(e.getMessage());
    }

    SendPhoto sendMessage = new SendPhoto();
    sendMessage.setCaption(text);
    sendMessage.setPhoto(new InputFile(poll.getPictureId()));
    sendMessage.setReplyMarkup(markup);
    sendMessage.setChatId(channelId);
    sendMessage.setCaptionEntities(entities);
    Message sentMessage = messageSender.sendPollToChannel(sendMessage);
    poll.setChannelMessageId(sentMessage.getMessageId());
    log.info("Kanal xabari saqlandi: pollId={}, channelMessageId={}", poll.getId(),
        sentMessage.getMessageId());

    pollRepository.save(poll);
  }

  private void sendPollToChannelEditMessage(Poll poll) {
    log.info("Kanal xabari yangilanmoqda: pollId={}, channelMessageId={}", poll.getId(),
        poll.getChannelMessageId());

    List<List<InlineKeyboardButton>> rows = new ArrayList<>();

    for (Long candidateId : poll.getCandidatesId()) {
      Candidate candidate = candidateRepository.findById(candidateId).orElseThrow();

      InlineKeyboardButton button = new InlineKeyboardButton();
      button.setText(candidate.getName() + " - " + candidate.getVoteCount());
      button.setUrl("https://t.me/" + botUsername + "?start=" + poll.getId());
      rows.add(List.of(button));
    }

    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
    markup.setKeyboard(rows);

    String caption = poll.getTitle();
    List<MessageEntity> entities = List.of();
    try {
      entities = objectMapper.readValue(
          poll.getTitleEntities(),
          new TypeReference<>() {
          }
      );
    } catch (JsonProcessingException e) {
      log.error(e.getMessage());
    }

    EditMessageCaption editMessage = new EditMessageCaption();
    editMessage.setMessageId(poll.getChannelMessageId());
    editMessage.setCaption(caption);
    editMessage.setReplyMarkup(markup);
    editMessage.setChatId(channelId);
    editMessage.setCaptionEntities(entities);
    messageSender.send(editMessage);

    log.info("Kanallar xabari muvaffaqiyatli yangilandi: pollId={}", poll.getId());
  }

  private SendMessage sendText(Long chatId, String text) {
    SendMessage message = new SendMessage();
    message.setChatId(chatId.toString());
    message.setText(text);

    return message;
  }

  private boolean isSubscribed(Long chatId, String channelLink) {
    try {
      GetChatMember getChatMember = new GetChatMember();
      getChatMember.setChatId(channelLink);
      getChatMember.setUserId(chatId);
      ChatMember member = messageSender.execute(getChatMember);
      String status = member.getStatus();
      boolean subscribed = status.equals("member")
          || status.equals("administrator")
          || status.equals("creator");
      if (!subscribed) {
        return false;
      }
    } catch (Exception e) {
      log.error("isSubscribed xatolik: chatId={}, channel={}", chatId, channelLink, e);
      return false;
    }

    return true;
  }

  public BotApiMethod<?> pressActivePolls(Update update, String callbackData) {
    List<Poll> pollList = pollRepository.findByActiveAndChannelMessageIdIsNotNull(true);
    Long chatId = update.getMessage().getChatId();

    List<List<InlineKeyboardButton>> rows = new ArrayList<>();

    for (Poll poll : pollList) {
      InlineKeyboardButton button = new InlineKeyboardButton();
      button.setText(poll.getTitle());
      button.setCallbackData(callbackData + poll.getId());
      List<InlineKeyboardButton> row = new ArrayList<>();
      row.add(button);
      rows.add(row);
    }

    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
    markup.setKeyboard(rows);
    SendMessage sendMessage = new SendMessage();
    sendMessage.setText("Active so'rovnomalar✅🔜");
    sendMessage.setChatId(chatId.toString());
    sendMessage.setReplyMarkup(markup);

    return sendMessage;
  }

  private BotApiMethod<?> pressExit(Update update) {
    Long chatId = update.getMessage().getChatId();
    log.info("So'rovnoma yaratish bekor qlindi: {} ", chatId);
    createPollStatus.put(chatId, false);
    stateHolder.remove(chatId);
    pollDrafts.remove(chatId);
    candidateDrafts.remove(chatId);
    channelDrafts.remove(chatId);
    return pressAdminPage(update);
  }

  private boolean isBotAdminInChannel(String channelUsername) {
    if (channelUsername == null || channelUsername.isBlank()
        || channelUsername.equals("@telegram")) {
      return false;
    }

    try {
      GetChatMember getChatMember = new GetChatMember();
      getChatMember.setChatId(channelUsername);
      getChatMember.setUserId(messageSender.getBotId());

      ChatMember member = messageSender.execute(getChatMember);

      if (member == null) {
        return false;
      }

      String status = member.getStatus();
      return status.equals("administrator") || status.equals("creator");

    } catch (Exception e) {
      log.error("Bot admin tekshirishda xatolik: channel={}", channelUsername, e);
      return false;
    }
  }

  private boolean isChannelExists(String channelUsername) {
    try {
      GetChat getChat = new GetChat();
      getChat.setChatId(channelUsername);

      messageSender.executeChat(getChat);
      return true;

    } catch (Exception e) {
      log.warn("Kanal topilmadi: channel={}", channelUsername);
      return false;
    }
  }

  private boolean isRateLimited(Long chatId) {
    LocalDateTime lastTime = voteRateLimit.get(chatId);
    if (lastTime != null && lastTime.plusSeconds(2).isAfter(LocalDateTime.now())) {
      return true;
    }
    voteRateLimit.put(chatId, LocalDateTime.now());
    return false;
  }
}


