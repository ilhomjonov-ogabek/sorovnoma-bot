package spring.boot.realtask.service.impl;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import spring.boot.realtask.dto.TransactionDTO;
import spring.boot.realtask.exceptions.ApiResponseErrorException;
import spring.boot.realtask.service.ExternalApiService;

@Service
@RequiredArgsConstructor
public class ExternalApiServiceImpl implements ExternalApiService {

private final WebClient webClient;

  @Override
  public Optional<TransactionDTO> getDTO(Long id) {

    TransactionDTO dto = webClient.get()
        .uri("/get-operation/{id}", id)
        .retrieve()
        .onStatus(
            HttpStatusCode::is4xxClientError,
            response -> Mono.error(
                new ApiResponseErrorException(
                    "Wrong request: " + response.statusCode()
                )
            )
        )
        .onStatus(
            HttpStatusCode::is5xxServerError,
            response -> Mono.error(
                new ApiResponseErrorException(
                    "API is not working: " + response.statusCode()
                )
            )
        )
        .bodyToMono(TransactionDTO.class)
        .block();

    return Optional.ofNullable(dto);
  }
}
