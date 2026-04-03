package spring.boot.workingwithapinew.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.boot.workingwithapinew.entity.Post;

public interface PostRepository extends JpaRepository<Post, Integer> {

}