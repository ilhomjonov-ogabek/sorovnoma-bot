package spring.boot.workingwithapinew.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "post")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {

  @Id
  @Column(name = "id", nullable = false)
  private int id;

  private int userId;
  private String title;
  private String body;

}