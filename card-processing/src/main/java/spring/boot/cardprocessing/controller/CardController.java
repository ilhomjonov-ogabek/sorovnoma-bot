package spring.boot.cardprocessing.controller;


import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import spring.boot.cardprocessing.dto.CardDto;
import spring.boot.cardprocessing.dto.CardDto.Response;
import spring.boot.cardprocessing.dto.TransactionDto;
import spring.boot.cardprocessing.dto.TransactionDto.PageResponse;
import spring.boot.cardprocessing.enums.TransactionType;
import spring.boot.cardprocessing.service.CardService;
import spring.boot.cardprocessing.service.TransactionService;

@RestController
@RequestMapping("/api/v1/cards")
@RequiredArgsConstructor
@Log4j2
@SecurityRequirement(name = "bearerAuth")
public class CardController {

  private final CardService cardService;
  private final TransactionService transactionService;

  @PostMapping()
  public ResponseEntity<?> createCard(@RequestHeader("Idempotency-Key") String idempotencyKey,
      @Valid @RequestBody CardDto.CreateRequest request) {
    log.info("Request <----- POST Card object create: {}", request);

    Response card = cardService.createCard(idempotencyKey, request);

    log.info("Response ----->POST Card object created: {}", card);

    return ResponseEntity.status(HttpStatus.CREATED).body(card);
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getCardById(@PathVariable UUID id) {
    log.info("Request <----- GET Card object : {}", id);
    Response card = cardService.getCard(id);
    String eTag = cardService.generateEtag(id);
    log.info("Response -----> Card object : {}", card);
    return ResponseEntity.status(HttpStatus.OK).header("ETag", eTag).body(card);
  }

  @PostMapping("/{id}/block")
  public ResponseEntity<?> blockCard(@PathVariable UUID id,
      @RequestHeader("If-Match") String ifMatch) {
    log.info("Request <----- POST Card block  : {}", id);
    cardService.blockCard(id, ifMatch);
    log.info("Response -----> Card blocked  :");
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/unblock")
  public ResponseEntity<?> unblockCard(@PathVariable UUID id,
      @RequestHeader("If-Match") String ifMatch) {
    log.info("Request <----- POST Card unblock  : {}", id);
    cardService.unblockCard(id, ifMatch);
    log.info("Response -----> Card unblocked  :");
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{cardId}/debit")
  public ResponseEntity<?> debit(@PathVariable UUID cardId,
      @RequestHeader("Idempotency-Key") String idempotencyKey,
      @Valid @RequestBody TransactionDto.DebitRequest request) {
    TransactionDto.Response response = transactionService.debit(cardId, idempotencyKey, request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/{cardId}/credit")
  public ResponseEntity<?> credit(@PathVariable UUID cardId,
      @RequestHeader("Idempotency-Key") String idempotencyKey,
      @Valid @RequestBody TransactionDto.CreditRequest request) {
    TransactionDto.Response response = transactionService.credit(cardId, idempotencyKey, request);
    return ResponseEntity.ok(response);
  }


  @GetMapping("/{cardId}/transactions")
  public ResponseEntity<?> getTransactions(@PathVariable UUID cardId,
      @RequestParam(required = false) TransactionType type,
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    PageResponse response = transactionService.getTransactions(cardId, type, page, size);
    return ResponseEntity.ok(response);
  }


}













