package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

 private final UserRepository userRepository;

 @Transactional
public User findByEmail(String email){
   User user = userRepository.findByEmail(email)
       .orElseThrow(() -> new RuntimeException("User not found"));
  return user;
}

public void save(User user){
  userRepository.save(user);
}

}
