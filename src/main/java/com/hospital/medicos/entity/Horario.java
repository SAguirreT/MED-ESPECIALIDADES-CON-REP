package com.hospital.medicos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "horarios")
public class Horario {
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

  @ManyToOne private Consultorio consultorio;

  public Consultorio getConsultorio() {
    return consultorio;
  }

  public void setConsultorio(Consultorio value) {
    this.consultorio = value;
  }

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DiaSemana diaSemana;

  public DiaSemana getDiaSemana() {
    return diaSemana;
  }

  public void setDiaSemana(DiaSemana value) {
    this.diaSemana = value;
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

  @Column(nullable = false)
  private Integer duracionCitaMinutos;

  public Integer getDuracionCitaMinutos() {
    return duracionCitaMinutos;
  }

  public void setDuracionCitaMinutos(Integer value) {
    this.duracionCitaMinutos = value;
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
