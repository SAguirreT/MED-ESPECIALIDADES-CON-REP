package com.hospital.medicos.service;

import com.hospital.medicos.entity.BloqueoHorario;
import com.hospital.medicos.entity.Horario;
import java.util.List;

public interface HorarioService {
    Horario registrarHorario(Long medicoId, Horario horario);              // RF-MED-11
    Horario actualizarHorario(Long id, Horario horario);                   // RF-MED-12
    List<Horario> obtenerPorMedico(Long medicoId);
    Horario obtenerPorId(Long id);

    BloqueoHorario bloquearHorario(Long medicoId, BloqueoHorario bloqueo); // RF-MED-13
    List<BloqueoHorario> obtenerBloqueosPorMedico(Long medicoId);
}