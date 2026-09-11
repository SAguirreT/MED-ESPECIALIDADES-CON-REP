package com.hospital.medicos.controller;

import com.hospital.medicos.dto.ReporteEspecialidadDTO;
import com.hospital.medicos.entity.Especialidad;
import com.hospital.medicos.repository.EspecialidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final EspecialidadRepository especialidadRepository;

    @Autowired
    public ReporteController(EspecialidadRepository especialidadRepository) {
        this.especialidadRepository = especialidadRepository;
    }

    // RF-MED-21
    @GetMapping("/medicos-por-especialidad")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ReporteEspecialidadDTO>> medicosPorEspecialidad() {
        List<Especialidad> especialidades = especialidadRepository.findAll();
        List<ReporteEspecialidadDTO> reporte = especialidades.stream()
                .map(e -> new ReporteEspecialidadDTO(
                        e.getNombre(),
                        e.getMedicos().stream().filter(m -> Boolean.TRUE.equals(m.getEstado())).count()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(reporte);
    }
}