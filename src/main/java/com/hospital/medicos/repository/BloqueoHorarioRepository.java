package com.hospital.medicos.repository;

import com.hospital.medicos.entity.BloqueoHorario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BloqueoHorarioRepository extends JpaRepository<BloqueoHorario, Long> {
  java.util.List<BloqueoHorario> findByMedicoId(Long id);
}
