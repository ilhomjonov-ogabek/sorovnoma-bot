package spring.boot.realtask.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import spring.boot.realtask.dto.TransactionDTO;
import spring.boot.realtask.entity.TransactionEntity;
import spring.boot.realtask.repository.TransactionEntityRepository;
import spring.boot.realtask.service.ConvertToPdfService;
import spring.boot.realtask.service.ExternalApiService;
import spring.boot.realtask.service.GenerateCheckService;
import spring.boot.realtask.service.WordTemplateService;

@Service
@RequiredArgsConstructor
public class GenerateCheckServiceImpl implements GenerateCheckService {

  private final ExternalApiService externalApiService;
  private final WordTemplateService wordTemplateService;
  private final ConvertToPdfService convertToPdfService;
  private final TransactionEntityRepository transactionEntityRepository;

  @Override
  public ResponseEntity<Resource> generateCheck(Long id) {
    byte[] word;
    Optional<TransactionEntity> byOperationId = transactionEntityRepository.findByOperationId(id);
    if (byOperationId.isPresent()) {
      word =getWord(id);
    }else {
      Optional<TransactionDTO> dto = externalApiService.getDTO(id);
      if (dto.isEmpty()) {
        return ResponseEntity.notFound().build();
      }
      word = wordTemplateService.editWord(dto);
      if (word == null||word.length==0) {
        return ResponseEntity.notFound().build();
      }
      TransactionEntity entity = TransactionEntity.builder()
          .operationId(id)
          .date(dto.get().getDate())
          .amount(dto.get().getAmount())
          .currency(dto.get().getCurrency())
          .terminal(dto.get().getTerminal())
          .templateId(dto.get().getTemplateId())
          .senderName(dto.get().getSenderName())
          .commission(dto.get().getCommission())
          .receiverName(dto.get().getReceiverName())
          .receiverCard(dto.get().getReceiverCard())
          .senderCard(dto.get().getSenderCard())
          .build();
      transactionEntityRepository.save(entity);
    }


    byte[] pdfBytes = convertToPdfService.convertToPdf(word);

    if (pdfBytes == null) {
      return ResponseEntity.notFound().build();
    }


    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=operation_"+id+".pdf")
        .contentLength(pdfBytes.length)
        .body(new ByteArrayResource(pdfBytes));
  }

  @Override
  public ResponseEntity<Resource> generateCheck(Optional<TransactionDTO> dto) {
    byte[] word;
    Optional<TransactionEntity> byOperationId = transactionEntityRepository.findByOperationId(dto.get()
        .getOperationId());
    if (byOperationId.isPresent()) {
      word =getWord(dto.get().getOperationId());
    }else {
      if (dto.isEmpty()) {
        return ResponseEntity.notFound().build();
      }
      word = wordTemplateService.editWord(dto);
      if (word == null||word.length==0) {
        return ResponseEntity.notFound().build();
      }
      TransactionEntity entity = TransactionEntity.builder()
          .operationId(dto.get().getOperationId())
          .date(dto.get().getDate())
          .amount(dto.get().getAmount())
          .currency(dto.get().getCurrency())
          .terminal(dto.get().getTerminal())
          .templateId(dto.get().getTemplateId())
          .senderName(dto.get().getSenderName())
          .commission(dto.get().getCommission())
          .receiverName(dto.get().getReceiverName())
          .receiverCard(dto.get().getReceiverCard())
          .senderCard(dto.get().getSenderCard())
          .build();
      transactionEntityRepository.save(entity);
    }


    byte[] pdfBytes = convertToPdfService.convertToPdf(word);

    if (pdfBytes == null) {
      return ResponseEntity.notFound().build();
    }


    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=operation_"+dto.get().getOperationId()+".pdf")
        .contentLength(pdfBytes.length)
        .body(new ByteArrayResource(pdfBytes));
  }

  private byte[] getWord(Long id){
    Path path = Paths.get("D:/G59IlhomjonovOgabek/real-task/words/operation_"+id+".pdf");
    byte[] fileBytes = new byte[0];

    try {
      fileBytes = Files.readAllBytes(path);
    } catch (IOException e) {
      System.out.println(e.getMessage());
    }
    return fileBytes;
  }

}
