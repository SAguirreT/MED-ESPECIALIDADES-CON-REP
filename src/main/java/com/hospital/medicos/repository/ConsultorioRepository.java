package com.hospital.medicos.repository;

import com.hospital.medicos.entity.Consultorio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultorioRepository extends JpaRepository<Consultorio, Long> {
  boolean existsByCodigoAndIdNot(String codigo, Long id);

  boolean existsByCodigo(String codigo);
}
