package spring.boot.sorovnomabot.repository;

import java.util.List;
import java.util.Optional;
import org.apache.poi.sl.draw.geom.GuideIf.Op;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import spring.boot.sorovnomabot.entity.User;
import spring.boot.sorovnomabot.enums.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByChatId(Long chatId);

  Optional<User> getByUsername(String username);

  Optional<User> getByUsernameAndRole(String username, UserRole userRole);

  @Query("SELECT u.chatId FROM User u WHERE u.role = :role1 OR u.role = :role2")
  List<Long> getChatIdsByRoles(@Param("role1") UserRole role1,
      @Param("role2") UserRole role2);

  List<User> getByRole(UserRole role);
}