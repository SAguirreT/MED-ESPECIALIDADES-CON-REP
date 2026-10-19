package com.hospital.medicos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "bloqueos_horario")
public class BloqueoHorario {
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
  private java.time.LocalDate fecha;

  public java.time.LocalDate getFecha() {
    return fecha;
  }

  public void setFecha(java.time.LocalDate value) {
    this.fecha = value;
  }

  @Column(nullable = false)
  private java.time.LocalTime horaInicio;

  public java.time.LocalTime getHoraInicio() {
    return horaInicio;
  }

  public void setHoraInicio(java.time.LocalTime value) {
    this.horaInicio = value;
  }

  @Column(nullable = false)
  private java.time.LocalTime horaFin;

  public java.time.LocalTime getHoraFin() {
    return horaFin;
  }

  public void setHoraFin(java.time.LocalTime value) {
    this.horaFin = value;
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
