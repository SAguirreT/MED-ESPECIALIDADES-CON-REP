package com.hospital.medicos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "pacientes")
public class Paciente {
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

  @Column(nullable = false, unique = true, length = 8)
  private String dni;

  public String getDni() {
    return dni;
  }

  public void setDni(String value) {
    this.dni = value;
  }

  @Column(nullable = false)
  private java.time.LocalDate fechaNacimiento;

  public java.time.LocalDate getFechaNacimiento() {
    return fechaNacimiento;
  }

  public void setFechaNacimiento(java.time.LocalDate value) {
    this.fechaNacimiento = value;
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
}
