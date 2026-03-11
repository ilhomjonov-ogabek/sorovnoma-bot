package spring.boot.cardprocessing.service;

import java.util.UUID;
import spring.boot.cardprocessing.dto.TransactionDto;
import spring.boot.cardprocessing.enums.TransactionType;

public interface TransactionService {

  TransactionDto.Response debit(UUID cardId, String idempotencyKey, TransactionDto.DebitRequest request);


  TransactionDto.Response credit(UUID cardId, String idempotencyKey, TransactionDto.CreditRequest request);


  TransactionDto.PageResponse getTransactions(UUID cardId, TransactionType type, int page, int size);

}
