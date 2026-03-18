package spring.boot.realtask.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import spring.boot.realtask.dto.TransactionDTO;
import spring.boot.realtask.service.WordTemplateService;

@Service
public class WordTemplateServiceImpl implements WordTemplateService {

  @Value("${app.storage.path}")
  private String storagePath;

  @Override
  public byte[] editWord(Optional<TransactionDTO> dto) {

    try {
//      ClassPathResource resource = new ClassPathResource(dto.get().getTemplateId()+".docx");
      ClassPathResource resource = new ClassPathResource("templates/template.docx");

      InputStream inputStream = resource.getInputStream();

      XWPFDocument document = new XWPFDocument(inputStream);

      XWPFDocument documentEdited = mapping(document, dto.get());

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      documentEdited.write(outputStream);

      documentEdited.close();
      inputStream.close();

      saveWordToDisk(dto.get().getOperationId(), outputStream.toByteArray());

      return outputStream.toByteArray();

    } catch (Exception e) {
      throw new RuntimeException("Word template yuklashda xatolik", e);
    }

  }

  private XWPFDocument mapping(XWPFDocument document, TransactionDTO dto) {

    Map<String, String> placeholders = new HashMap<>();
    placeholders.put("${operation_id}", String.valueOf(dto.getOperationId()));
    placeholders.put("${sender_card}", dto.getSenderCard());
    placeholders.put("${sender_name}", dto.getSenderName());
    placeholders.put("${receiver_card}", dto.getReceiverCard());
    placeholders.put("${receiver_name}", dto.getReceiverName());
    placeholders.put("${date}", dto.getDate().toString());
    placeholders.put("${amount}", dto.getAmount().toString());
    placeholders.put("${send_amount}", dto.getAmount().toString());
    placeholders.put("${commission}", (dto.getCommission().multiply(dto.getAmount())).toString());


    for (XWPFParagraph paragraph : document.getParagraphs()) {
      replaceTextInParagraph(paragraph, placeholders);
    }

    for (XWPFTable table : document.getTables()) {
      for (XWPFTableRow row : table.getRows()) {
        for (XWPFTableCell cell : row.getTableCells()) {
          for (XWPFParagraph paragraph : cell.getParagraphs()) {
            replaceTextInParagraph(paragraph, placeholders);
          }
        }
      }
    }

    return document;
  }

  private void replaceTextInParagraph(XWPFParagraph paragraph, Map<String, String> placeholders) {
    StringBuilder sb = new StringBuilder();
    for (XWPFRun run : paragraph.getRuns()) {
      String text = run.getText(0);
      if (text != null) {
        sb.append(text);
      }
    }

    String fullText = sb.toString();
    if (fullText.isEmpty()) return;

    String replacedText = fullText;
    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
      replacedText = replacedText.replace(entry.getKey(), entry.getValue());
    }

    if (!replacedText.equals(fullText)) {

      int runCount = paragraph.getRuns().size();
      for (int i = runCount - 1; i >= 0; i--) {
        paragraph.removeRun(i);
      }

      XWPFRun newRun = paragraph.createRun();
      newRun.setText(replacedText);
    }
  }

  public Path saveWordToDisk(Long operationId, byte[] wordBytes) {
    try {
      Path dir = Paths.get(storagePath);
      Files.createDirectories(dir);

      String fileName = "operation_" + operationId + ".pdf";
      Path filePath = dir.resolve(fileName);

      Files.write(filePath, wordBytes);

      return filePath;
    } catch (IOException e) {
      throw new RuntimeException("Wordni diskka saqlashda xatolik", e);
    }
  }
}
