package com.hospital.medicos.service;

import com.hospital.medicos.entity.Consultorio;
import com.hospital.medicos.entity.Horario;
import java.util.List;

public interface ConsultorioService {
    Consultorio registrarConsultorio(Consultorio consultorio);      // RF-MED-15
    List<Consultorio> obtenerTodos();
    Consultorio obtenerPorId(Long id);
    Horario asignarConsultorioAHorario(Long horarioId, Long consultorioId); // RF-MED-16
}