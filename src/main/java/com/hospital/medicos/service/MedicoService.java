package com.hospital.medicos.service;

import com.hospital.medicos.entity.Medico;
import java.util.List;

public interface MedicoService {
    Medico registrarMedico(Medico medico);                             // RF-MED-01
    Medico actualizarMedico(Long id, Medico medico);                  // RF-MED-02
    List<Medico> obtenerTodos();                                       // RF-MED-03
    Medico obtenerPorId(Long id);                                      // RF-MED-03
    Medico cambiarEstado(Long id, Boolean estado);                     // RF-MED-04
    Medico asociarEspecialidades(Long medicoId, List<Long> especialidadIds); // RF-MED-06
}