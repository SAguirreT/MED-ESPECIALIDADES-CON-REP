package com.hospital.medicos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "vacaciones_permisos")
public class VacacionPermiso {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @ManyToOne(optional = false)
  private Medico medico;

  public Medico getMedico() {
    return medico;
  }

  public void setMedico(Medico value) {
    this.medico = value;
  }

  @Column(nullable = false)
  private java.time.LocalDate fechaInicio;

  public java.time.LocalDate getFechaInicio() {
    return fechaInicio;
  }

  public void setFechaInicio(java.time.LocalDate value) {
    this.fechaInicio = value;
  }

  @Column(nullable = false)
  private java.time.LocalDate fechaFin;

  public java.time.LocalDate getFechaFin() {
    return fechaFin;
  }

  public void setFechaFin(java.time.LocalDate value) {
    this.fechaFin = value;
  }

  @Column(nullable = false, length = 255)
  private String motivo;

  public String getMotivo() {
    return motivo;
  }

  public void setMotivo(String value) {
    this.motivo = value;
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
