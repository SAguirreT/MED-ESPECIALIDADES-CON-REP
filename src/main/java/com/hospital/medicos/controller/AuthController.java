package com.hospital.medicos.controller;

import com.hospital.medicos.dto.Requests.*;
import com.hospital.medicos.entity.User;
import com.hospital.medicos.security.JwtUtils;
import com.hospital.medicos.service.*;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthenticationManager manager;
  private final UserService users;
  private final JwtUtils jwt;
  private final AuditService audit;

  public AuthController(
      AuthenticationManager manager, UserService users, JwtUtils jwt, AuditService audit) {
    this.manager = manager;
    this.users = users;
    this.jwt = jwt;
    this.audit = audit;
  }

  @PostMapping("/signin")
  public Map<String, Object> signin(@Valid @RequestBody Login input) {
    var auth =
        manager.authenticate(
            new UsernamePasswordAuthenticationToken(
                input.email().trim().toLowerCase(Locale.ROOT), input.password()));
    var u = users.current(auth.getName());
    audit.recordAs(u.getEmail(), "INICIAR_SESION", "usuarios", u.getId(), "Autenticación correcta");
    return Map.of(
        "accessToken",
        jwt.generateJwtToken(u),
        "tokenType",
        "Bearer",
        "expiresIn",
        jwt.expirationSeconds(),
        "id",
        u.getId(),
        "username",
        u.getUsername(),
        "email",
        u.getEmail(),
        "roles",
        u.getRoles().stream().map(r -> "ROLE_" + r.getName()).toList());
  }

  @PostMapping("/signup")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, String> signup(@Valid @RequestBody Signup input) {
    users.requestAccess(input.username(), input.email(), input.password());
    return Map.of("message", "Solicitud registrada. Un administrador debe activar tu cuenta.");
  }

  @GetMapping("/me")
  public User me(Principal principal) {
    return users.current(principal.getName());
  }

  @PostMapping("/signout")
  public Map<String, String> signout(Principal p) {
    users.logout(p.getName());
    return Map.of("message", "Sesiones cerradas");
  }

  @PutMapping("/password")
  public Map<String, String> password(Principal p, @Valid @RequestBody PasswordChange input) {
    users.password(p.getName(), input);
    return Map.of("message", "Contraseña actualizada. Inicia sesión nuevamente.");
  }
}
