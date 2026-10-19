package com.hospital.medicos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "auditoria")
public class Auditoria {
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
  private String usuario;

  public String getUsuario() {
    return usuario;
  }

  public void setUsuario(String value) {
    this.usuario = value;
  }

  @Column(nullable = false, length = 30)
  private String accion;

  public String getAccion() {
    return accion;
  }

  public void setAccion(String value) {
    this.accion = value;
  }

  @Column(nullable = false, length = 60)
  private String tabla;

  public String getTabla() {
    return tabla;
  }

  public void setTabla(String value) {
    this.tabla = value;
  }

  private Long registroId;

  public Long getRegistroId() {
    return registroId;
  }

  public void setRegistroId(Long value) {
    this.registroId = value;
  }

  @Column(nullable = false)
  private java.time.OffsetDateTime fechaHora;

  public java.time.OffsetDateTime getFechaHora() {
    return fechaHora;
  }

  public void setFechaHora(java.time.OffsetDateTime value) {
    this.fechaHora = value;
  }

  @Column(nullable = false, length = 255)
  private String detalle;

  public String getDetalle() {
    return detalle;
  }

  public void setDetalle(String value) {
    this.detalle = value;
  }
}
