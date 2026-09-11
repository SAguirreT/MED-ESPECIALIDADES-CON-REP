package com.hospital.medicos.service;

import com.hospital.medicos.dto.SlotDisponibilidadDTO;
import java.time.LocalDate;
import java.util.List;

public interface DisponibilidadService {
    List<SlotDisponibilidadDTO> calcularDisponibilidad(Long medicoId, LocalDate fecha); // RF-MED-18
}