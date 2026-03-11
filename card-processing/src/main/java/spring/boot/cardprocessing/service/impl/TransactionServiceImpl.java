package spring.boot.cardprocessing.service.impl;


import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import spring.boot.cardprocessing.dto.TransactionDto;
import spring.boot.cardprocessing.dto.TransactionDto.CreditRequest;
import spring.boot.cardprocessing.dto.TransactionDto.DebitRequest;
import spring.boot.cardprocessing.dto.TransactionDto.PageResponse;
import spring.boot.cardprocessing.dto.TransactionDto.Response;
import spring.boot.cardprocessing.entity.Card;
import spring.boot.cardprocessing.entity.IdempotencyKey;
import spring.boot.cardprocessing.entity.Transaction;
import spring.boot.cardprocessing.enums.Currency;
import spring.boot.cardprocessing.enums.TransactionType;
import spring.boot.cardprocessing.exception.InsufficientFundsException;
import spring.boot.cardprocessing.repository.CardRepository;
import spring.boot.cardprocessing.repository.IdempotencyKeyRepository;
import spring.boot.cardprocessing.repository.TransactionRepository;
import spring.boot.cardprocessing.service.CardService;
import spring.boot.cardprocessing.service.CbuService;
import spring.boot.cardprocessing.service.TransactionService;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;


import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class TransactionServiceImpl implements TransactionService {

  private final TransactionRepository transactionRepository;
  private final CardRepository cardRepository;
  private final IdempotencyKeyRepository idempotencyKeyRepository;
  private final CardService cardService;
  private final CbuService cbuService;
  private final ObjectMapper objectMapper;


  @Override
  @Transactional
  public Response debit(UUID cardId, String idempotencyKey, DebitRequest request) {
    long amount = request.getAmount();
    Optional<IdempotencyKey> existing = idempotencyKeyRepository
        .findByIdempotencyKey(idempotencyKey);
    if (existing.isPresent()) {
      try {
        return objectMapper.readValue(
            existing.get().getResponseBody(),
            Response.class
        );
      } catch (JsonProcessingException e) {
        log.error("Parse error: {}", e.getMessage());
      }
    }
    Card card = cardService.findActiveCard(cardId);

    Long exchangeRate = null;
    if (request.getCurrency() != card.getCurrency()) {
      exchangeRate = cbuService.getExchangeRate(
          request.getCurrency(),
          card.getCurrency()
      );
      amount = convertAmount(request.getAmount(), request.getCurrency(), card.getCurrency(),
          exchangeRate);
    }

    if (card.getBalance() < amount) {
      log.warn("Insufficient funds: cardId: {}, balance: {}, amount: {}",
          cardId, card.getBalance(), amount);
      throw new InsufficientFundsException("Card balance is not enough");
    }

    card.setBalance(card.getBalance() - amount);
    cardRepository.save(card);

    Transaction transaction = Transaction.builder()
        .externalId(request.getExternalId())
        .card(card)
        .type(TransactionType.DEBIT)
        .amount(request.getAmount())
        .afterBalance(card.getBalance())
        .currency(request.getCurrency())
        .purpose(request.getPurpose())
        .exchangeRate(exchangeRate)
        .build();

    transactionRepository.save(transaction);

    Response response = toResponse(transaction, card);

    try {
      IdempotencyKey idempotencyRecord = IdempotencyKey.builder()
          .idempotencyKey(idempotencyKey)
          .endpoint("DEBIT")
          .responseBody(objectMapper.writeValueAsString(response))
          .statusCode(200)
          .build();
      idempotencyKeyRepository.save(idempotencyRecord);
    } catch (JsonProcessingException e) {
      log.error("Idempotency save error: {}", e.getMessage());
    }

    return response;
  }

  @Override
  @Transactional
  public Response credit(UUID cardId, String idempotencyKey, CreditRequest request) {
    long amount = request.getAmount();
    Optional<IdempotencyKey> existing = idempotencyKeyRepository
        .findByIdempotencyKey(idempotencyKey);
    if (existing.isPresent()) {
      try {
        return objectMapper.readValue(
            existing.get().getResponseBody(),
            Response.class
        );
      } catch (JsonProcessingException e) {
        log.error("Parse error: {}", e.getMessage());
      }
    }
    Card card = cardService.findActiveCard(cardId);

    Long exchangeRate = null;
    if (request.getCurrency() != card.getCurrency()) {
      exchangeRate = cbuService.getExchangeRate(
          request.getCurrency(),
          card.getCurrency()
      );
      amount = convertAmount(request.getAmount(), request.getCurrency(), card.getCurrency(),
          exchangeRate);
    }

    card.setBalance(card.getBalance() + amount);
    cardRepository.save(card);

    Transaction transaction = Transaction.builder()
        .externalId(request.getExternalId())
        .card(card)
        .type(TransactionType.CREDIT)
        .amount(request.getAmount())
        .afterBalance(card.getBalance())
        .currency(request.getCurrency())
        .purpose(null)
        .exchangeRate(exchangeRate)
        .build();

    transactionRepository.save(transaction);

    Response response = toResponse(transaction, card);

    try {
      IdempotencyKey idempotencyRecord = IdempotencyKey.builder()
          .idempotencyKey(idempotencyKey)
          .endpoint("CREDIT")
          .responseBody(objectMapper.writeValueAsString(response))
          .statusCode(200)
          .build();
      idempotencyKeyRepository.save(idempotencyRecord);
    } catch (JsonProcessingException e) {
      log.error("Idempotency save error: {}", e.getMessage());
    }

    return response;
  }

  @Override
  public PageResponse getTransactions(UUID cardId, TransactionType type, int page, int size) {
    PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());


    Page<Transaction> transactions;
    if (type != null) {
      transactions = transactionRepository.findAllByCard_CardIdAndType(cardId, type, pageable);
    } else {
      transactions = transactionRepository.findAllByCard_CardId(cardId, pageable);
    }


    PageResponse response = new PageResponse();
    response.setPage(page);
    response.setSize(size);
    response.setTotalPages(transactions.getTotalPages());
    response.setTotalItems(transactions.getTotalElements());
    response.setContent(
        transactions.getContent()
            .stream()
            .map(t -> toResponse(t, t.getCard()))
            .collect(Collectors.toList())
    );

    return response;
  }


  private long convertAmount(long amount, Currency from, Currency to, Long exchangeRate) {

    if (from == to) {
      return amount;
    }

    if (from == Currency.USD && to == Currency.UZS) {
      return amount * exchangeRate / 100L;
    } else {
      return amount * 100L / exchangeRate;
    }
  }

  private TransactionDto.Response toResponse(Transaction transaction, Card card) {
    TransactionDto.Response response = new TransactionDto.Response();
    response.setTransactionId(transaction.getTransactionId().toString());
    response.setExternalId(transaction.getExternalId());
    response.setCardId(card.getCardId().toString());
    response.setAmount(transaction.getAmount());
    response.setAfterBalance(transaction.getAfterBalance());
    response.setCurrency(transaction.getCurrency());
    response.setPurpose(transaction.getPurpose());
    response.setExchangeRate(transaction.getExchangeRate());
    response.setType(transaction.getType());
    return response;
  }
}
