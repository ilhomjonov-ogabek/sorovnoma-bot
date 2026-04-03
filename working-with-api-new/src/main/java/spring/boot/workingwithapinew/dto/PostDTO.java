package spring.boot.workingwithapinew.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for {@link spring.boot.workingwithapinew.entity.Post}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDTO implements Serializable {

  private int id;
  private int userId;
  private String title;
  private String body;
}