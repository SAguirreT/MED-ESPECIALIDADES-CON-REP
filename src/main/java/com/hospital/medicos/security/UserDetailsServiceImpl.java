package com.hospital.medicos.security;

import com.hospital.medicos.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
  private final UserRepository users;

  public UserDetailsServiceImpl(UserRepository users) {
    this.users = users;
  }

  public UserDetails loadUserByUsername(String email) {
    var u =
        users
            .findByEmail(email.trim().toLowerCase(java.util.Locale.ROOT))
            .orElseThrow(() -> new UsernameNotFoundException("Credenciales incorrectas"));
    return org.springframework.security.core.userdetails.User.withUsername(u.getEmail())
        .password(u.getPassword())
        .disabled(!u.getActivo())
        .roles(u.getRoles().stream().map(r -> r.getName()).toArray(String[]::new))
        .build();
  }
}
