package spring.boot.realtask.service;

import java.util.Optional;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import spring.boot.realtask.dto.TransactionDTO;

public interface GenerateCheckService {

  byte[] generateCheck(Long id);
  ResponseEntity<Resource> generateCheck(Optional<TransactionDTO> dto);
}
