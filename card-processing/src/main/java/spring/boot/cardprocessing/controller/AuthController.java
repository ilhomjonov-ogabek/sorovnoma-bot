package spring.boot.cardprocessing.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import spring.boot.cardprocessing.security.JwtUtil;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Log4j2
public class AuthController {

  private final JwtUtil jwtUtil;

  @PostMapping("/token")
  public ResponseEntity<?> getToken(@RequestBody Map<String, Long> body) {
    Long userId = body.get("userId");

    if (userId == null) {
      return ResponseEntity.badRequest().body(Map.of("message", "userId is required"));
    }

    log.info("Generating token for userId: {}", userId);
    String token = jwtUtil.generateToken(userId);

    return ResponseEntity.ok(Map.of("token", token));
  }
}
