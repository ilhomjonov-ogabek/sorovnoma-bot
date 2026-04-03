package spring.boot.sorovnomabot.repository;

import java.util.List;
import java.util.Optional;
import org.apache.poi.sl.draw.geom.GuideIf.Op;
import org.springframework.data.jpa.repository.JpaRepository;
import spring.boot.sorovnomabot.entity.User;
import spring.boot.sorovnomabot.enums.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByChatId(Long chatId);

  Optional<User> getByUsername(String username);

  Optional<User> getByUsernameAndRole(String username, UserRole userRole);
}