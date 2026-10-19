package com.hospital.medicos.security;

import com.hospital.medicos.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/** Only installed in the security chain, not also registered as a servlet filter. */
public class JwtAuthFilter extends OncePerRequestFilter {
  private final JwtUtils jwt;
  private final UserRepository users;

  public JwtAuthFilter(JwtUtils jwt, UserRepository users) {
    this.jwt = jwt;
    this.users = users;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      try {
        var claims = jwt.validateJwtToken(header.substring(7));
        users
            .findByEmail(claims.getSubject())
            .filter(u -> Boolean.TRUE.equals(u.getActivo()))
            .filter(u -> u.getTokenVersion().equals(claims.get("version", Integer.class)))
            .ifPresent(
                u -> {
                  var roles =
                      u.getRoles().stream()
                          .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getName()))
                          .toList();
                  SecurityContextHolder.getContext()
                      .setAuthentication(
                          new UsernamePasswordAuthenticationToken(u.getEmail(), null, roles));
                });
      } catch (JwtException | IllegalArgumentException e) {
        SecurityContextHolder.clearContext();
      }
    }
    chain.doFilter(request, response);
  }
}
