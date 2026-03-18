package spring.boot.realtask.controller;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
@Log4j2
public class GenerateCheckController {
  private final GenerateCheckService generateCheckService;

  @GetMapping("/{id}")
  public ResponseEntity<Resource> getCheck(@PathVariable Long id){
    log.info("Get check resource");

    byte[] bytes = generateCheckService.generateCheck(id);

    log.info("Generated check resource");

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=operation_"+id+".pdf")
        .contentLength(bytes.length)
        .body(new ByteArrayResource(bytes));
  }

  @PostMapping("/generate")
  public ResponseEntity<?> generateCheck(@RequestBody TransactionDTO transactionDTO){
    log.info("Generate check resource");
    ResponseEntity<Resource> resourceResponseEntity = generateCheckService.generateCheck(Optional.ofNullable(transactionDTO));
    log.info("Check generated resource");
    return resourceResponseEntity;
  }



}
