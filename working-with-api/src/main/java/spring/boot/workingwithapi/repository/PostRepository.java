package spring.boot.workingwithapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.boot.workingwithapi.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

}