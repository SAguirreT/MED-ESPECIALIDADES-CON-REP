package com.hospital.medicos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "especialidades")
public class Especialidad {
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

  @Column(length = 255)
  private String descripcion;

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String value) {
    this.descripcion = value;
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
