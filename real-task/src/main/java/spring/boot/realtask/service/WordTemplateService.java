package spring.boot.realtask.service;

import java.util.Optional;
import spring.boot.realtask.dto.TransactionDTO;

public interface WordTemplateService {


  byte[] editWord(Optional<TransactionDTO> dto);
}
