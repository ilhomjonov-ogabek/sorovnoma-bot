package spring.boot.realtask.service.impl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.stereotype.Service;
import spring.boot.realtask.service.ConvertToPdfService;

@Service
public class ConvertToPdfServiceImpl implements ConvertToPdfService {

  @Override
  public byte[] convertToPdf(byte[] file) {

    try {
      Path tempDocx = Files.createTempFile("input-", ".docx");
      Files.write(tempDocx, file);

      ProcessBuilder command = new ProcessBuilder(
          "C:\\Program Files\\LibreOffice\\program\\soffice.exe",
          "--headless",
          "--convert-to", "pdf",
          "--outdir", tempDocx.getParent().toString(),
          tempDocx.toString()
      );

      Process process = command.start();
      int exitCode = process.waitFor();

      if (exitCode != 0) {
        throw new RuntimeException("Process exited with code " + exitCode);
      }

      Path pdfPath = Paths.get(
          tempDocx.toString().replace(".docx", ".pdf")
      );

      if (!Files.exists(pdfPath) || Files.size(pdfPath) == 0) {
        throw new RuntimeException("PDF file not created or empty");
      }

      byte[] pdfBytes = Files.readAllBytes(pdfPath);

      Files.deleteIfExists(tempDocx);
      Files.deleteIfExists(pdfPath);

     return pdfBytes;

    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return new byte[0];
  }
}

