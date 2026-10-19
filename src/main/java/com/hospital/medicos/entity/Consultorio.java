package com.hospital.medicos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "consultorios")
public class Consultorio {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Column(nullable = false, unique = true, length = 20)
  private String codigo;

  public String getCodigo() {
    return codigo;
  }

  public void setCodigo(String value) {
    this.codigo = value;
  }

  @Column(nullable = false, length = 100)
  private String nombre;

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String value) {
    this.nombre = value;
  }

  @Column(length = 20)
  private String piso;

  public String getPiso() {
    return piso;
  }

  public void setPiso(String value) {
    this.piso = value;
  }

  @Column(length = 100)
  private String area;

  public String getArea() {
    return area;
  }

  public void setArea(String value) {
    this.area = value;
  }

  @Column(nullable = false, length = 20)
  private String estado;

  public String getEstado() {
    return estado;
  }

  public void setEstado(String value) {
    this.estado = value;
  }
}
