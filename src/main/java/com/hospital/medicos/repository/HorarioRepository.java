package com.hospital.medicos.repository;

import com.hospital.medicos.entity.Horario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HorarioRepository extends JpaRepository<Horario, Long> {
  java.util.List<Horario> findByMedicoId(Long id);
}
