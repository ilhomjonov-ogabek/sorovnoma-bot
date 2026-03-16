package spring.boot.returnjson;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/get-operation")
public class Controller {

  @GetMapping("/{id}")
  public ResponseEntity<DTO> get(@PathVariable Long id) {
    DTO dto = DTO.builder()
        .operationId(id)
        .templateId("template")
        .amount(BigDecimal.valueOf(1000))
        .currency("UZS")
        .terminal("484989")
        .date(LocalDate.now())
        .senderCard("9860*****2632")
        .receiverCard("5614****2525")
        .commission(BigDecimal.valueOf(0.1))
        .senderName("Ilhomjonov Ogabek")
        .receiverName("Islomjonov Jaxongir")
    .build();
    return ResponseEntity.ok(dto);
  }

}
