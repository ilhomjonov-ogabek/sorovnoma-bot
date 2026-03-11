package spring.boot.cardprocessing.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import javax.naming.LimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;


import java.util.UUID;
import spring.boot.cardprocessing.dto.CardDto;
import spring.boot.cardprocessing.dto.CardDto.Response;
import spring.boot.cardprocessing.entity.Card;
import spring.boot.cardprocessing.entity.IdempotencyKey;
import spring.boot.cardprocessing.enums.CardStatus;
import spring.boot.cardprocessing.enums.Currency;
import spring.boot.cardprocessing.exception.CardNotFoundException;
import spring.boot.cardprocessing.exception.EtagMismatchException;
import spring.boot.cardprocessing.exception.IncompatibleStatusException;
import spring.boot.cardprocessing.repository.CardRepository;
import spring.boot.cardprocessing.repository.IdempotencyKeyRepository;
import spring.boot.cardprocessing.service.CardService;

@Service
@RequiredArgsConstructor
@Log4j2
public class CardServiceImpl implements CardService {

  private  final CardRepository cardRepository;
  private  final IdempotencyKeyRepository idempotencyKeyRepository;
  private  final ObjectMapper objectMapper;


  @Override
  public CardDto.Response createCard(String idempotencyKey, CardDto.CreateRequest request)  {

    Optional<IdempotencyKey> existing = idempotencyKeyRepository
        .findByIdempotencyKey(idempotencyKey);
    if (existing.isPresent()) {
      try {
        return objectMapper.readValue(
            existing.get().getResponseBody(),
            CardDto.Response.class
        );
      } catch (JsonProcessingException e) {
        log.error("Parse error: {}", e.getMessage());
      }
    }

    long cardCount = cardRepository.countNonClosedCardsByUserId(request.getUserId());
    if (cardCount >= 3) {
       new LimitExceededException("User already has maximum 3 non-closed cards");
    }

    Card card = Card.builder()
        .userId(request.getUserId())
        .status(request.getStatus() != null ? request.getStatus() : CardStatus.ACTIVE)
        .balance(request.getInitialAmount() != null ? request.getInitialAmount() : 0L)
        .currency(request.getCurrency() != null ? request.getCurrency() : Currency.UZS)
        .build();

    cardRepository.save(card);

    Response response = toResponse(card);

    try {
      IdempotencyKey idempotencyRecord = IdempotencyKey.builder()
          .idempotencyKey(idempotencyKey)
          .endpoint("CREATE_CARD")
          .responseBody(objectMapper.writeValueAsString(response))
          .statusCode(201)
          .build();
      idempotencyKeyRepository.save(idempotencyRecord);
    } catch (JsonProcessingException e) {
      log.error("Idempotency save error: {}", e.getMessage());
    }

    return response;
  }

  @Override
  public Response getCard(UUID cardId) {
    Card card = cardRepository.findById(cardId)
        .orElseThrow(() -> {
          log.error("Card not found: {}", cardId);
          return new CardNotFoundException("Card not found");
        });

    return toResponse(card);
  }

  @Override
  public void blockCard(UUID cardId, String ifMatchEtag) {
    Card card = cardRepository.findById(cardId)
        .orElseThrow(() -> {
          log.error("Card not found: {}", cardId);
          return new CardNotFoundException("Card not found");
        });

    String expectedEtag = "\"" + card.getCardId().toString() + "\"";
    if (!expectedEtag.equals(ifMatchEtag)) {
      throw new EtagMismatchException("ETag mismatch");
    }

    if(card.getStatus() != CardStatus.ACTIVE) {
      log.warn("Card is not ACTIVE: {} | status: {}", cardId, card.getStatus());
      throw new IncompatibleStatusException(
          "Card status must be ACTIVE to block, current status: " + card.getStatus()
      );
    }

    card.setStatus(CardStatus.BLOCKED);
    cardRepository.save(card);

    log.info("Card blocked: {}", cardId);
  }

  @Override
  public void unblockCard(UUID cardId, String ifMatchEtag) {
    Card card = cardRepository.findById(cardId)
        .orElseThrow(() -> {
          log.error("Card not found: {}", cardId);
          return new CardNotFoundException("Card not found");
        });

    String expectedEtag = "\"" + card.getCardId().toString() + "\"";
    if (!expectedEtag.equals(ifMatchEtag)) {
      throw new EtagMismatchException("ETag mismatch");
    }

    if(card.getStatus() != CardStatus.BLOCKED) {
      log.warn("Card is not BLOCKED: {} | status: {}", cardId, card.getStatus());
      throw new IncompatibleStatusException(
          "Card status must be BLOCKED to active, current status: " + card.getStatus()
      );
    }

    card.setStatus(CardStatus.ACTIVE);
    cardRepository.save(card);

    log.info("Card activeted: {}", cardId);
  }

  @Override
  public Card findActiveCard(UUID cardId) {
    Card card = cardRepository.findById(cardId)
        .orElseThrow(() -> {
          log.error("Card not found: {}", cardId);
          return new CardNotFoundException("Card not found");
        });
    if(card.getStatus() != CardStatus.ACTIVE) {
      log.warn("Card is not ACTIVE: {} | status: {}", cardId, card.getStatus());
      throw new IncompatibleStatusException("Card status must be ACTIVE to perform this operation, current status: " + card.getStatus());
    }
    return card;
  }

  @Override
  public Response toResponse(Card card) {
    CardDto.Response response = new CardDto.Response();
    response.setCardId(card.getCardId().toString());
    response.setUserId(card.getUserId());
    response.setStatus(card.getStatus());
    response.setBalance(card.getBalance());
    response.setCurrency(card.getCurrency());
    return response;
  }

  @Override
  public String generateEtag(UUID cardId) {
    return "\"" + cardId + "\"";
  }


}
