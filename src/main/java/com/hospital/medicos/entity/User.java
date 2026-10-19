package com.hospital.medicos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Column(nullable = false, unique = true, length = 60)
  private String username;

  public String getUsername() {
    return username;
  }

  public void setUsername(String value) {
    this.username = value;
  }

  @Column(nullable = false, unique = true, length = 100)
  private String email;

  public String getEmail() {
    return email;
  }

  public void setEmail(String value) {
    this.email = value;
  }

  @com.fasterxml.jackson.annotation.JsonIgnore
  @Column(nullable = false)
  private String password;

  public String getPassword() {
    return password;
  }

  public void setPassword(String value) {
    this.password = value;
  }

  @Column(nullable = false)
  private Boolean activo = true;

  public Boolean getActivo() {
    return activo;
  }

  public void setActivo(Boolean value) {
    this.activo = value;
  }

  @com.fasterxml.jackson.annotation.JsonIgnore
  @Column(nullable = false)
  private Integer tokenVersion = 0;

  public Integer getTokenVersion() {
    return tokenVersion;
  }

  public void setTokenVersion(Integer value) {
    this.tokenVersion = value;
  }

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "usuario_roles",
      joinColumns = @JoinColumn(name = "usuario_id"),
      inverseJoinColumns = @JoinColumn(name = "rol_id"))
  private java.util.Set<Role> roles = new java.util.HashSet<>();

  public java.util.Set<Role> getRoles() {
    return roles;
  }

  public void setRoles(java.util.Set<Role> value) {
    this.roles = value;
  }
}
