package com.hospital.medicos.controller;

import com.hospital.medicos.entity.BloqueoHorario;
import com.hospital.medicos.entity.Horario;
import com.hospital.medicos.service.HorarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios")
public class HorarioController {

    private final HorarioService horarioService;

    @Autowired
    public HorarioController(HorarioService horarioService) {
        this.horarioService = horarioService;
    }

    // RF-MED-11: Registrar horario de un médico
    @PostMapping("/medico/{medicoId}")
    public ResponseEntity<Horario> registrar(@PathVariable Long medicoId, @Valid @RequestBody Horario horario) {
        return new ResponseEntity<>(horarioService.registrarHorario(medicoId, horario), HttpStatus.CREATED);
    }

    // RF-MED-12: Modificar horario
    @PutMapping("/{id}")
    public ResponseEntity<Horario> actualizar(@PathVariable Long id, @Valid @RequestBody Horario horario) {
        return ResponseEntity.ok(horarioService.actualizarHorario(id, horario));
    }

    @GetMapping("/medico/{medicoId}")
    public ResponseEntity<List<Horario>> obtenerPorMedico(@PathVariable Long medicoId) {
        return ResponseEntity.ok(horarioService.obtenerPorMedico(medicoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Horario> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(horarioService.obtenerPorId(id));
    }

    // RF-MED-13: Bloquear un horario puntual
    @PostMapping("/medico/{medicoId}/bloqueos")
    public ResponseEntity<BloqueoHorario> bloquear(@PathVariable Long medicoId, @Valid @RequestBody BloqueoHorario bloqueo) {
        return new ResponseEntity<>(horarioService.bloquearHorario(medicoId, bloqueo), HttpStatus.CREATED);
    }

    @GetMapping("/medico/{medicoId}/bloqueos")
    public ResponseEntity<List<BloqueoHorario>> obtenerBloqueos(@PathVariable Long medicoId) {
        return ResponseEntity.ok(horarioService.obtenerBloqueosPorMedico(medicoId));
    }
}