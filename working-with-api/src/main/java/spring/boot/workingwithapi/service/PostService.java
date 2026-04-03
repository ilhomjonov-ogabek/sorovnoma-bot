package spring.boot.workingwithapi.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import spring.boot.workingwithapi.entity.Post;
import spring.boot.workingwithapi.repository.PostRepository;

@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;


  public boolean saveAll(List<Post> posts) {
    for (Post post : posts) {
      System.out.println(post);
    }
    postRepository.saveAll(posts);
    return true;
  }
}
