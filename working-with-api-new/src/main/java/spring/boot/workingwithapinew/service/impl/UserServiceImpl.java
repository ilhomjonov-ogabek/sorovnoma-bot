package spring.boot.workingwithapinew.service.impl;


import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import spring.boot.workingwithapinew.dto.PostDTO;
import spring.boot.workingwithapinew.dto.UserDTO;
import spring.boot.workingwithapinew.entity.Post;
import spring.boot.workingwithapinew.entity.User;
import spring.boot.workingwithapinew.exception.UserNotFoundException;
import spring.boot.workingwithapinew.repository.PostRepository;
import spring.boot.workingwithapinew.repository.UserRepository;
import spring.boot.workingwithapinew.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final WebClient webClient;
  private final UserRepository userRepository;
  private final PostRepository postRepository;

  @Override
  public int savePosts(Long id) {

    int postCount = 0;

    UserDTO block = webClient.get()
        .uri(uriBuilder -> uriBuilder.path("/users/{id}").build(id))
        .retrieve()
        .onStatus(
            HttpStatusCode::is4xxClientError,
            response -> Mono.error(new UserNotFoundException("Bu id boyicha user topilmadi!"))
        )
        .bodyToMono(UserDTO.class)
        .block();

      User user = User.builder()
          .id(block.getId())
          .name(block.getName())
          .email(block.getEmail())
          .build();
      userRepository.save(user);


    List<PostDTO> list = webClient.get()
        .uri(uriBuilder -> uriBuilder
            .path("/posts")
            .queryParam("userId", id)
            .build())
        .retrieve()
        .onStatus(HttpStatusCode::is4xxClientError,
            response -> Mono.empty()
        )
        .bodyToFlux(PostDTO.class)
        .collectList()
        .defaultIfEmpty(Collections.emptyList())
        .block();

    for (PostDTO postDTO : list) {
      Post post = Post.builder()
          .id(postDTO.getId())
          .userId(postDTO.getUserId())
          .title(postDTO.getTitle())
          .body(postDTO.getBody())
          .build();
      postCount++;
      postRepository.save(post);

    }

    return postCount;
  }
}
