package com.hospital.medicos.service;

import com.hospital.medicos.dto.Requests.*;
import com.hospital.medicos.entity.*;
import com.hospital.medicos.repository.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class UserService {
  private final UserRepository users;
  private final RoleRepository roles;
  private final PasswordEncoder encoder;
  private final AuditService audit;
  private final AgendaLockRepository lock;

  public UserService(
      UserRepository users,
      RoleRepository roles,
      PasswordEncoder encoder,
      AuditService audit,
      AgendaLockRepository lock) {
    this.users = users;
    this.roles = roles;
    this.encoder = encoder;
    this.audit = audit;
    this.lock = lock;
  }

  public User create(String username, String email, String password, Set<String> roleNames) {
    checkPassword(password);
    email = email.trim().toLowerCase(Locale.ROOT);
    username = username.trim();
    if (users.existsByEmail(email) || users.existsByUsername(username))
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "El correo o nombre de usuario ya existe");
    var u = new User();
    u.setUsername(username);
    u.setEmail(email);
    u.setPassword(encoder.encode(password));
    u.setRoles(resolve(roleNames));
    users.save(u);
    audit.record(
        "CREAR", "usuarios", u.getId(), "Cuenta creada con roles " + String.join(", ", roleNames));
    return u;
  }

  public User requestAccess(String username, String email, String password) {
    var u = create(username, email, password, Set.of("USER"));
    u.setActivo(false);
    return u;
  }

  private void checkPassword(String password) {
    if (password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "La contraseña no puede superar 72 bytes UTF-8");
  }

  private Set<Role> resolve(Set<String> names) {
    var result = new HashSet<Role>();
    for (String n : names)
      result.add(
          roles
              .findByName(n)
              .orElseThrow(
                  () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no permitido")));
    return result;
  }

  public User access(Long id, UserAccess input) {
    lock.acquire();
    var u =
        users
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    boolean admin = u.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"));
    if (admin
        && u.getActivo()
        && (!input.activo() || !input.roles().contains("ADMIN"))
        && users.findAll().stream()
                .filter(
                    x ->
                        x.getActivo()
                            && x.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN")))
                .count()
            <= 1)
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Debe permanecer al menos un administrador activo");
    u.setActivo(input.activo());
    u.setRoles(resolve(input.roles()));
    u.setTokenVersion(u.getTokenVersion() + 1);
    audit.record(
        "CAMBIAR_ACCESO",
        "usuarios",
        id,
        "Activo=" + input.activo() + "; roles=" + String.join(",", input.roles()));
    return u;
  }

  public User current(String email) {
    return users
        .findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesión inválida"));
  }

  public void logout(String email) {
    var u = current(email);
    u.setTokenVersion(u.getTokenVersion() + 1);
    audit.record("CERRAR_SESION", "usuarios", u.getId(), "Se revocaron los tokens del usuario");
  }

  public void password(String email, PasswordChange input) {
    checkPassword(input.nueva());
    var u = current(email);
    if (!encoder.matches(input.actual(), u.getPassword()))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual no coincide");
    u.setPassword(encoder.encode(input.nueva()));
    u.setTokenVersion(u.getTokenVersion() + 1);
    audit.record(
        "CAMBIAR_CLAVE", "usuarios", u.getId(), "Contraseña actualizada; sesiones revocadas");
  }
}
