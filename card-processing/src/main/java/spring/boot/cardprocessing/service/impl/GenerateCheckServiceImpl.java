package spring.boot.cardprocessing.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import spring.boot.cardprocessing.config.WebClientConfig;
import spring.boot.cardprocessing.dto.CheckDTO;
import spring.boot.cardprocessing.exception.CheckGenerationException;
import spring.boot.cardprocessing.service.GenerateCheckService;

@Service
@RequiredArgsConstructor
public class GenerateCheckServiceImpl implements GenerateCheckService {

  private final WebClientConfig webClient;

  @Override
  public byte[] generateCheck(CheckDTO transactionDTO) {

    byte[] body = webClient.webClient().post()
        .uri("http://localhost:9090/api/convert/generate")
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_PDF)
        .bodyValue(transactionDTO)
        .retrieve()
        .onStatus(
            HttpStatusCode::is4xxClientError,
            response -> Mono.error(
                new CheckGenerationException(
                    "Wrong request: " + response.statusCode()
                )
            )
        )
        .onStatus(
            HttpStatusCode::is5xxServerError,
            response -> Mono.error(
                new CheckGenerationException(
                    "PDF service don't working: " + response.statusCode()
                )
            )
        )
        .bodyToMono(byte[].class)
        .block();

    return body;
  }
}
