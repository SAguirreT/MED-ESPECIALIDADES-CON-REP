package com.hospital.medicos.config;

import com.hospital.medicos.repository.UserRepository;
import com.hospital.medicos.security.*;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.*;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  AuthenticationManager authenticationManager(
      UserDetailsServiceImpl users, PasswordEncoder encoder) {
    var provider = new DaoAuthenticationProvider(users);
    provider.setPasswordEncoder(encoder);
    return new ProviderManager(provider);
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http, JwtUtils jwt, UserRepository users)
      throws Exception {
    return http.csrf(c -> c.disable()) // Bearer header only; no ambient cookie authentication.
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .headers(
            h ->
                h.contentSecurityPolicy(
                    c ->
                        c.policyDirectives(
                            "default-src 'self'; script-src 'self'; style-src 'self'; img-src"
                                + " 'self' data:; connect-src 'self'; frame-ancestors 'none';"
                                + " base-uri 'self'; form-action 'self'")))
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(
                        (req, res, ex) -> {
                          res.setStatus(401);
                          res.setContentType("application/json;charset=UTF-8");
                          res.getWriter()
                              .write(
                                  "{\"status\":401,\"message\":\"Inicia sesión con un token válido"
                                      + " y vigente\"}");
                        })
                    .accessDeniedHandler(
                        (req, res, ex) -> {
                          res.setStatus(403);
                          res.setContentType("application/json;charset=UTF-8");
                          res.getWriter()
                              .write(
                                  "{\"status\":403,\"message\":\"Tu rol no permite realizar esta"
                                      + " acción\"}");
                        }))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(
                        "/",
                        "/index.html",
                        "/assets/**",
                        "/favicon.ico",
                        "/error",
                        "/api/test/all",
                        "/api/auth/signin",
                        "/api/auth/signup",
                        "/api/info")
                    .permitAll()
                    .requestMatchers("/api/auth/**")
                    .authenticated()
                    .requestMatchers(
                        "/api/usuarios/**", "/api/roles/**", "/api/auditoria/**", "/api/test/admin")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/test/mod")
                    .hasAnyRole("ADMIN", "MODERATOR")
                    .requestMatchers("/api/test/user")
                    .hasAnyRole("ADMIN", "MODERATOR", "USER")
                    .requestMatchers(HttpMethod.GET, "/api/**")
                    .hasAnyRole("ADMIN", "MODERATOR", "USER")
                    .requestMatchers("/api/pacientes/**", "/api/citas/**")
                    .hasAnyRole("ADMIN", "MODERATOR")
                    .requestMatchers("/api/**")
                    .hasRole("ADMIN")
                    .anyRequest()
                    .denyAll())
        .addFilterBefore(new JwtAuthFilter(jwt, users), UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
