package com.hospital.medicos.controller;

import com.hospital.medicos.dto.SlotDisponibilidadDTO;
import com.hospital.medicos.service.DisponibilidadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/disponibilidad")
public class DisponibilidadController {

    private final DisponibilidadService disponibilidadService;

    @Autowired
    public DisponibilidadController(DisponibilidadService disponibilidadService) {
        this.disponibilidadService = disponibilidadService;
    }

    // RF-MED-18: GET /api/disponibilidad/medico/1?fecha=2026-09-15
    @GetMapping("/medico/{medicoId}")
    public ResponseEntity<List<SlotDisponibilidadDTO>> obtenerDisponibilidad(
            @PathVariable Long medicoId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(disponibilidadService.calcularDisponibilidad(medicoId, fecha));
    }
}