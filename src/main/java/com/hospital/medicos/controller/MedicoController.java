package com.hospital.medicos.controller;

import com.hospital.medicos.entity.Medico;
import com.hospital.medicos.service.MedicoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medicos")
public class MedicoController {

    private final MedicoService medicoService;

    @Autowired
    public MedicoController(MedicoService medicoService) {
        this.medicoService = medicoService;
    }

    @PostMapping
    public ResponseEntity<Medico> registrarMedico(@Valid @RequestBody Medico medico) {
        return new ResponseEntity<>(medicoService.registrarMedico(medico), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Medico> actualizarMedico(@PathVariable Long id, @Valid @RequestBody Medico medico) {
        return ResponseEntity.ok(medicoService.actualizarMedico(id, medico));
    }

    @GetMapping
    public ResponseEntity<List<Medico>> obtenerTodos() {
        return ResponseEntity.ok(medicoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Medico> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(medicoService.obtenerPorId(id));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<Medico> cambiarEstado(@PathVariable Long id, @RequestParam Boolean estado) {
        return ResponseEntity.ok(medicoService.cambiarEstado(id, estado));
    }

    @PostMapping("/{medicoId}/especialidades")
    public ResponseEntity<Medico> asociarEspecialidades(
            @PathVariable Long medicoId,
            @RequestBody List<Long> especialidadIds) {
        return ResponseEntity.ok(medicoService.asociarEspecialidades(medicoId, especialidadIds));
    }
}