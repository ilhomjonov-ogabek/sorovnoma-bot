package spring.boot.workingwithapinew.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.boot.workingwithapinew.entity.User;

public interface UserRepository extends JpaRepository<User, Integer> {

}