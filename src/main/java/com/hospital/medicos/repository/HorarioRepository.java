package com.hospital.medicos.repository;

import com.hospital.medicos.entity.DiaSemana;
import com.hospital.medicos.entity.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Long> {
    List<Horario> findByMedicoId(Long medicoId);
    List<Horario> findByConsultorioIdAndDiaSemana(Long consultorioId, DiaSemana diaSemana);
}