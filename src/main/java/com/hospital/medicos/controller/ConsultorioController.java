package com.hospital.medicos.controller;

import com.hospital.medicos.entity.Consultorio;
import com.hospital.medicos.entity.Horario;
import com.hospital.medicos.service.ConsultorioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultorios")
public class ConsultorioController {

    private final ConsultorioService consultorioService;

    @Autowired
    public ConsultorioController(ConsultorioService consultorioService) {
        this.consultorioService = consultorioService;
    }

    // RF-MED-15
    @PostMapping
    public ResponseEntity<Consultorio> registrar(@Valid @RequestBody Consultorio consultorio) {
        return new ResponseEntity<>(consultorioService.registrarConsultorio(consultorio), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Consultorio>> obtenerTodos() {
        return ResponseEntity.ok(consultorioService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Consultorio> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(consultorioService.obtenerPorId(id));
    }

    // RF-MED-16
    @PatchMapping("/horario/{horarioId}/asignar/{consultorioId}")
    public ResponseEntity<Horario> asignar(@PathVariable Long horarioId, @PathVariable Long consultorioId) {
        return ResponseEntity.ok(consultorioService.asignarConsultorioAHorario(horarioId, consultorioId));
    }
}