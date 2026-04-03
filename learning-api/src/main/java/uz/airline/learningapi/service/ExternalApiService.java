package uz.airline.learningapi.service;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import uz.airline.learningapi.dto.PostDTO;

@Service
@RequiredArgsConstructor
public class ExternalApiService {

  @Qualifier("webClient")
  private final WebClient webClient;

  @Qualifier("newWebClient")
  private final WebClient newWebClient;

  public List<PostDTO> getPosts() {
    try {
      List<PostDTO> list = webClient
          .get()
          .uri("/posts")
          .retrieve()
          .onStatus(
              HttpStatusCode::is4xxClientError,
              clientResponse -> Mono.error(new RuntimeException("wrong request"))
          )
          .onStatus(
              HttpStatusCode::is5xxServerError,
              clientResponse -> Mono.error(new RuntimeException("Server error"))
          )
          .bodyToFlux(PostDTO.class)
          .collectList()
          .block();

      return list;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return Collections.emptyList();
  }

  public boolean savePosts() {
    try {
      List<PostDTO> posts = getPosts();
      Boolean block = newWebClient.post()
          .uri("/save")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(posts)
          .retrieve()
          .onStatus(
              HttpStatusCode::is4xxClientError,
              clientResponse -> Mono.error(new RuntimeException("wrong request"))
          )
          .onStatus(
              HttpStatusCode::is5xxServerError,
              clientResponse -> Mono.error(new RuntimeException("Server error"))
          )
          .bodyToMono(Boolean.class)
          .block();
      return block;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }
}
