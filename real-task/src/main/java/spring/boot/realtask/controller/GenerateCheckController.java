package spring.boot.realtask.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.boot.realtask.service.GenerateCheckService;

@RestController
@RequestMapping("/api/convert")
@RequiredArgsConstructor
public class GenerateCheckController {
  private final GenerateCheckService generateCheckService;

  @GetMapping("/{id}")
  public ResponseEntity<Resource> getCheck(@PathVariable Long id){
    return generateCheckService.generateCheck(id);
  }



}
