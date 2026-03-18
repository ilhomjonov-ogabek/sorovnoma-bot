package spring.boot.realtask.controller;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.boot.realtask.dto.TransactionDTO;
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

  @PostMapping("/generate")
  public ResponseEntity<?> generateCheck(@RequestBody TransactionDTO transactionDTO){
    System.out.println(transactionDTO);
    ResponseEntity<Resource> resourceResponseEntity = generateCheckService.generateCheck(Optional.ofNullable(transactionDTO));
    return resourceResponseEntity;
  }



}
