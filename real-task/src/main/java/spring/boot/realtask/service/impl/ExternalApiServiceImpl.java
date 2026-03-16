package spring.boot.realtask.service.impl;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import spring.boot.realtask.dto.TransactionDTO;
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
        .bodyToMono(TransactionDTO.class)
        .block();

    return Optional.ofNullable(dto);
  }
}
