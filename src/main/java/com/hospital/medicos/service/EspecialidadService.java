
package com.hospital.medicos.service;

import com.hospital.medicos.entity.Especialidad;
import java.util.List;

public interface EspecialidadService {
    Especialidad registrarEspecialidad(Especialidad especialidad); // RF-MED-07
    Especialidad actualizarEspecialidad(Long id, Especialidad especialidad); // RF-MED-08
    List<Especialidad> obtenerTodas();
    Especialidad obtenerPorId(Long id);
    Especialidad cambiarEstado(Long id, Boolean estado); // RF-MED-09
}