package com.hospital.medicos.repository;

import com.hospital.medicos.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
  boolean existsByDniAndIdNot(String dni, Long id);

  boolean existsByDni(String dni);
}
