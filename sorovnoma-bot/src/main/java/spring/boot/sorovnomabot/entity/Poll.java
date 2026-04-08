package spring.boot.sorovnomabot.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.cglib.core.Local;

@Getter
@Setter
@Entity
@Table(name = "poll")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Poll {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @Column(columnDefinition = "TEXT")
  private String title;

  private String pictureId;

  private boolean active;

  private LocalDate creationDate;

  private LocalDate startDate;

  private LocalDate finishedDate;

  private List<Long> candidatesId;

  private List<Long> usersId;

  private List<String> channellsId;

  private Integer channelMessageId;

  @Column(columnDefinition = "TEXT")
  private String titleEntities;


  @PrePersist
  protected void onCreate() {
    this.creationDate = LocalDate.now();
  }



}