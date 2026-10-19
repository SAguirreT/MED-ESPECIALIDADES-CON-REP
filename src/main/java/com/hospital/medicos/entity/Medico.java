package com.hospital.medicos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "medicos")
public class Medico {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Column(nullable = false, length = 100)
  private String nombre;

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String value) {
    this.nombre = value;
  }

  @Column(nullable = false, length = 100)
  private String apellido;

  public String getApellido() {
    return apellido;
  }

  public void setApellido(String value) {
    this.apellido = value;
  }

  @Column(nullable = false, unique = true, length = 20)
  private String cmp;

  public String getCmp() {
    return cmp;
  }

  public void setCmp(String value) {
    this.cmp = value;
  }

  @Column(length = 20)
  private String telefono;

  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String value) {
    this.telefono = value;
  }

  @Column(length = 100)
  private String email;

  public String getEmail() {
    return email;
  }

  public void setEmail(String value) {
    this.email = value;
  }

  @Column(nullable = false)
  private Boolean estado = true;

  public Boolean getEstado() {
    return estado;
  }

  public void setEstado(Boolean value) {
    this.estado = value;
  }

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "medico_especialidad",
      joinColumns = @JoinColumn(name = "medico_id"),
      inverseJoinColumns = @JoinColumn(name = "especialidad_id"),
      uniqueConstraints = @UniqueConstraint(columnNames = {"medico_id", "especialidad_id"}))
  private java.util.List<Especialidad> especialidades = new java.util.ArrayList<>();

  public java.util.List<Especialidad> getEspecialidades() {
    return especialidades;
  }

  public void setEspecialidades(java.util.List<Especialidad> value) {
    this.especialidades = value;
  }
}
