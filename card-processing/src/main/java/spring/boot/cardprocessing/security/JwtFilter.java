package spring.boot.cardprocessing.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Log4j2
public class JwtFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authHeader.substring(7);

    if (jwtUtil.isTokenValid(token)) {
      Long userId = jwtUtil.extractUserId(token);
      log.debug("JWT valid, userId: {}", userId);

      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(userId, null, List.of());

      SecurityContextHolder.getContext().setAuthentication(authentication);
    } else {
      log.warn("Invalid JWT token");
    }

    filterChain.doFilter(request, response);
  }
}
































































