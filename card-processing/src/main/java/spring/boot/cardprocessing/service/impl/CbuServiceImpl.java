package spring.boot.cardprocessing.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import org.springframework.web.reactive.function.client.WebClient;
import spring.boot.cardprocessing.enums.Currency;
import spring.boot.cardprocessing.service.CbuService;

@Service
@RequiredArgsConstructor
@Log4j2
public class CbuServiceImpl implements CbuService {

  private final WebClient webClient;

  @Value("${cbu.api.url}")
  private String cbuApiUrl;

  @Override
  @Cacheable(value = "exchangeRates", key = "#from.name()")
  public Long getExchangeRate(Currency from, Currency to) {

    log.info("Fetching exchange rate from CBU: {} -> {}", from, to);

    try {
      List<Map<String, Object>> rates = webClient.get()
          .uri(cbuApiUrl)
          .retrieve()
          .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {
          })
          .block();

      if (rates == null || rates.isEmpty()) {
        throw new RuntimeException("CBU API returned empty response");
      }

      String targetCurrency = from == Currency.USD ? "USD" : to.name();

      return rates.stream()
          .filter(rate -> targetCurrency.equals(rate.get("Ccy")))
          .findFirst()
          .map(rate -> {
            // "12650.96" → 1265096 (tiyin)
            double rateValue = Double.parseDouble(rate.get("Rate").toString());
            return Math.round(rateValue * 100);
          })
          .orElseThrow(() -> new RuntimeException(
              "Exchange rate not found for: " + targetCurrency
          ));

    } catch (Exception e) {
      log.error("Failed to fetch exchange rate from CBU: {}", e.getMessage());
      throw new RuntimeException("Failed to fetch exchange rate: " + e.getMessage());
    }

  }
}
