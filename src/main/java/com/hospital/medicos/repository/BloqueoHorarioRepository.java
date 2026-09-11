package com.hospital.medicos.repository;

import com.hospital.medicos.entity.BloqueoHorario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BloqueoHorarioRepository extends JpaRepository<BloqueoHorario, Long> {
    List<BloqueoHorario> findByMedicoId(Long medicoId);
}