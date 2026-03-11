package spring.boot.cardprocessing.service;

import spring.boot.cardprocessing.enums.Currency;

public interface CbuService {

  Long getExchangeRate(Currency from, Currency to);

}
