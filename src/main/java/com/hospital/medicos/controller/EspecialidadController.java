package com.hospital.medicos.controller;

import com.hospital.medicos.entity.Especialidad;
import com.hospital.medicos.service.EspecialidadService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/especialidades")
public class EspecialidadController {

    private final EspecialidadService especialidadService;

    @Autowired
    public EspecialidadController(EspecialidadService especialidadService) {
        this.especialidadService = especialidadService;
    }

    // RF-MED-07
    @PostMapping
    public ResponseEntity<Especialidad> registrar(@Valid @RequestBody Especialidad especialidad) {
        return new ResponseEntity<>(especialidadService.registrarEspecialidad(especialidad), HttpStatus.CREATED);
    }

    // RF-MED-08
    @PutMapping("/{id}")
    public ResponseEntity<Especialidad> actualizar(@PathVariable Long id, @Valid @RequestBody Especialidad especialidad) {
        return ResponseEntity.ok(especialidadService.actualizarEspecialidad(id, especialidad));
    }

    @GetMapping
    public ResponseEntity<List<Especialidad>> obtenerTodas() {
        return ResponseEntity.ok(especialidadService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Especialidad> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(especialidadService.obtenerPorId(id));
    }

    // RF-MED-09
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Especialidad> cambiarEstado(@PathVariable Long id, @RequestParam Boolean estado) {
        return ResponseEntity.ok(especialidadService.cambiarEstado(id, estado));
    }
}