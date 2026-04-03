package uz.airline.learningapi.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.airline.learningapi.dto.PostDTO;
import uz.airline.learningapi.service.ExternalApiService;

import java.nio.file.Path;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
public class ExternalApiController {
  private final ExternalApiService externalApiService;



  @GetMapping("/posts")
  public ResponseEntity<List<PostDTO>> getPosts() {
    List<PostDTO> posts = externalApiService.getPosts();
    return ResponseEntity.ok(posts);
  }

  @PostMapping("/save")
  public ResponseEntity<?> savePosts(){
    boolean saved = externalApiService.savePosts();
    return ResponseEntity.ok(saved);
  }

}
