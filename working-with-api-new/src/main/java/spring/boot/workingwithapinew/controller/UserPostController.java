package spring.boot.workingwithapinew.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.boot.workingwithapinew.service.UserService;

@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserPostController {

  private final UserService userService;

  @PostMapping("/{id}/save-posts")
  public ResponseEntity<?> savePost(@PathVariable Long id){
    int i = userService.savePosts(id);
    return ResponseEntity.ok().body(i);
  }

}
