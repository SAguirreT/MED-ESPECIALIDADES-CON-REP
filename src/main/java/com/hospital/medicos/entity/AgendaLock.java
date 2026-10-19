package com.hospital.medicos.entity;

import jakarta.persistence.*;

/** A shared database row serializes scheduling mutations across application instances. */
@Entity
@Table(name = "agenda_lock")
public class AgendaLock {
  @Id private Long id;

  public AgendaLock() {}

  public AgendaLock(Long id) {
    this.id = id;
  }
}
