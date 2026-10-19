package com.hospital.medicos.repository;

import com.hospital.medicos.entity.Cita;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CitaRepository extends JpaRepository<Cita, Long> {
  java.util.List<Cita> findByFechaBetweenOrderByFechaAscHoraInicioAsc(
      java.time.LocalDate desde, java.time.LocalDate hasta);
}
