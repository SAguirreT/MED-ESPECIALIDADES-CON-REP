package com.hospital.medicos.repository;

import com.hospital.medicos.entity.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {
  boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

  boolean existsByNombreIgnoreCase(String nombre);
}
