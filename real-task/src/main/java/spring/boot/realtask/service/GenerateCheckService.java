package spring.boot.realtask.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface GenerateCheckService {

  ResponseEntity<Resource> generateCheck(Long id);
}
