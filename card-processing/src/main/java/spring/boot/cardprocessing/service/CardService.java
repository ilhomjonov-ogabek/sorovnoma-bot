package spring.boot.cardprocessing.service;


import java.util.UUID;
import spring.boot.cardprocessing.dto.CardDto;
import spring.boot.cardprocessing.entity.Card;

public interface CardService {

  CardDto.Response createCard(String idempotencyKey, CardDto.CreateRequest request);


  CardDto.Response getCard(UUID cardId);


  void blockCard(UUID cardId, String ifMatchEtag);


  void unblockCard(UUID cardId, String ifMatchEtag);


  Card findActiveCard(UUID cardId);


  CardDto.Response toResponse(Card card);

  String generateEtag(UUID cardId);
}
