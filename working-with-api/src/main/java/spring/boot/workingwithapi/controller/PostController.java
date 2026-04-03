package spring.boot.workingwithapi.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.boot.workingwithapi.entity.Post;
import spring.boot.workingwithapi.service.PostService;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;

  @PostMapping("/save")
  public ResponseEntity<?> savePosts(@RequestBody List<Post> posts) {
    boolean saved = postService.saveAll(posts);
    return ResponseEntity.ok().body(saved);
  }


}
