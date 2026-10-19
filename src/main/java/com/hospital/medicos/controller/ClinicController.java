package com.hospital.medicos.controller;

import com.hospital.medicos.dto.Requests.*;
import com.hospital.medicos.entity.*;
import com.hospital.medicos.repository.*;
import com.hospital.medicos.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ClinicController {
  private final ClinicService service;
  private final UserService users;
  private final UserRepository userRepo;
  private final RoleRepository roles;
  private final AuditoriaRepository audit;
  private final boolean demo;

  public ClinicController(
      ClinicService service,
      UserService users,
      UserRepository userRepo,
      RoleRepository roles,
      AuditoriaRepository audit,
      @Value("${app.demo}") boolean demo) {
    this.service = service;
    this.users = users;
    this.userRepo = userRepo;
    this.roles = roles;
    this.audit = audit;
    this.demo = demo;
  }

  @GetMapping("/info")
  public Object info() {
    return Map.of("nombre", "Clínica Aurora", "demo", demo, "zonaHoraria", "America/Lima");
  }

  @GetMapping("/dashboard")
  public Object dashboard() {
    return service.dashboard();
  }

  @GetMapping("/medicos")
  public Object medicos() {
    return service.medicos();
  }

  @GetMapping("/medicos/{id}")
  public Object medico(@PathVariable Long id) {
    return service.medico(id);
  }

  @PostMapping("/medicos")
  @ResponseStatus(HttpStatus.CREATED)
  public Object createMedico(@Valid @RequestBody MedicoInput d) {
    return service.saveMedico(null, d);
  }

  @PutMapping("/medicos/{id}")
  public Object updateMedico(@PathVariable Long id, @Valid @RequestBody MedicoInput d) {
    return service.saveMedico(id, d);
  }

  @PostMapping("/medicos/{id}/especialidades")
  public Object associate(@PathVariable Long id, @RequestBody List<@NotNull Long> ids) {
    return service.associate(id, ids, false);
  }

  @PutMapping("/medicos/{id}/especialidades")
  public Object replace(@PathVariable Long id, @RequestBody List<@NotNull Long> ids) {
    return service.associate(id, ids, true);
  }

  @GetMapping("/especialidades")
  public Object especialidades() {
    return service.especialidades();
  }

  @GetMapping("/especialidades/{id}")
  public Object especialidad(@PathVariable Long id) {
    return service.especialidad(id);
  }

  @PostMapping("/especialidades")
  @ResponseStatus(HttpStatus.CREATED)
  public Object createEspecialidad(@Valid @RequestBody EspecialidadInput d) {
    return service.saveEspecialidad(null, d);
  }

  @PutMapping("/especialidades/{id}")
  public Object updateEspecialidad(@PathVariable Long id, @Valid @RequestBody EspecialidadInput d) {
    return service.saveEspecialidad(id, d);
  }

  @GetMapping("/consultorios")
  public Object consultorios() {
    return service.consultorios();
  }

  @GetMapping("/consultorios/{id}")
  public Object consultorio(@PathVariable Long id) {
    return service.consultorio(id);
  }

  @PostMapping("/consultorios")
  @ResponseStatus(HttpStatus.CREATED)
  public Object createConsultorio(@Valid @RequestBody ConsultorioInput d) {
    return service.saveConsultorio(null, d);
  }

  @PutMapping("/consultorios/{id}")
  public Object updateConsultorio(@PathVariable Long id, @Valid @RequestBody ConsultorioInput d) {
    return service.saveConsultorio(id, d);
  }

  @PatchMapping("/consultorios/horario/{id}/asignar/{room}")
  public Object assign(@PathVariable Long id, @PathVariable Long room) {
    return service.assign(id, room);
  }

  @GetMapping("/pacientes")
  public Object pacientes() {
    return service.pacientes();
  }

  @GetMapping("/pacientes/{id}")
  public Object paciente(@PathVariable Long id) {
    return service.paciente(id);
  }

  @PostMapping("/pacientes")
  @ResponseStatus(HttpStatus.CREATED)
  public Object createPaciente(@Valid @RequestBody PacienteInput d) {
    return service.savePaciente(null, d);
  }

  @PutMapping("/pacientes/{id}")
  public Object updatePaciente(@PathVariable Long id, @Valid @RequestBody PacienteInput d) {
    return service.savePaciente(id, d);
  }

  @PatchMapping({
    "/medicos/{id}/estado",
    "/especialidades/{id}/estado",
    "/consultorios/{id}/estado",
    "/pacientes/{id}/estado",
    "/horarios/{id}/estado"
  })
  public Object state(
      @PathVariable Long id,
      @RequestParam boolean estado,
      jakarta.servlet.http.HttpServletRequest request) {
    return service.state(request.getRequestURI().split("/")[2], id, estado);
  }

  @GetMapping("/horarios")
  public Object horarios() {
    return service.horarios();
  }

  @GetMapping("/horarios/{id}")
  public Object horario(@PathVariable Long id) {
    return service.horario(id);
  }

  @GetMapping("/horarios/medico/{id}")
  public Object horariosMedico(@PathVariable Long id) {
    service.medico(id);
    return service.horarios().stream().filter(h -> h.getMedico().getId().equals(id)).toList();
  }

  @PostMapping("/horarios/medico/{id}")
  @ResponseStatus(HttpStatus.CREATED)
  public Object createHorario(@PathVariable Long id, @Valid @RequestBody HorarioInput d) {
    return service.saveHorario(null, id, d);
  }

  @PutMapping("/horarios/{id}")
  public Object updateHorario(@PathVariable Long id, @Valid @RequestBody HorarioInput d) {
    return service.saveHorario(id, service.horario(id).getMedico().getId(), d);
  }

  @GetMapping("/bloqueos")
  public Object bloqueos() {
    return service.bloqueos();
  }

  @GetMapping("/horarios/medico/{id}/bloqueos")
  public Object bloqueosMedico(@PathVariable Long id) {
    service.medico(id);
    return service.bloqueos().stream().filter(b -> b.getMedico().getId().equals(id)).toList();
  }

  @PostMapping("/horarios/medico/{id}/bloqueos")
  @ResponseStatus(HttpStatus.CREATED)
  public Object block(@PathVariable Long id, @Valid @RequestBody BloqueoInput d) {
    return service.block(id, d);
  }

  @PatchMapping("/bloqueos/{id}/cancelar")
  public Object unblock(@PathVariable Long id) {
    return service.cancelAbsence("bloqueos", id);
  }

  @GetMapping("/vacaciones")
  public Object vacaciones() {
    return service.vacaciones();
  }

  @PostMapping("/vacaciones")
  @ResponseStatus(HttpStatus.CREATED)
  public Object vacation(@Valid @RequestBody VacacionInput d) {
    return service.vacation(d);
  }

  @PatchMapping("/vacaciones/{id}/cancelar")
  public Object cancelVacation(@PathVariable Long id) {
    return service.cancelAbsence("vacaciones", id);
  }

  @GetMapping("/disponibilidad/medico/{id}")
  public Object availability(@PathVariable Long id, @RequestParam LocalDate fecha) {
    return service.availability(id, fecha);
  }

  @GetMapping("/citas")
  public Object citas(@RequestParam LocalDate desde, @RequestParam LocalDate hasta) {
    return service.citas(desde, hasta);
  }

  @PostMapping("/citas")
  @ResponseStatus(HttpStatus.CREATED)
  public Object cita(@Valid @RequestBody CitaInput d) {
    return service.saveCita(null, d);
  }

  @PutMapping("/citas/{id}")
  public Object reschedule(@PathVariable Long id, @Valid @RequestBody CitaInput d) {
    return service.saveCita(id, d);
  }

  @PatchMapping("/citas/{id}/cancelar")
  public Object cancelCita(@PathVariable Long id, @Valid @RequestBody Cancelacion d) {
    return service.cancelCita(id, d.motivo());
  }

  @PatchMapping("/citas/{id}/atender")
  public Object completeCita(@PathVariable Long id) {
    return service.completeCita(id);
  }

  @GetMapping("/reportes/medicos-por-especialidad")
  public Object report() {
    return service.report();
  }

  @GetMapping("/usuarios")
  public Object users() {
    return userRepo.findAll();
  }

  @GetMapping("/roles")
  public Object roles() {
    return roles.findAll();
  }

  @PostMapping("/usuarios")
  @ResponseStatus(HttpStatus.CREATED)
  public Object user(@Valid @RequestBody UserInput d) {
    return users.create(d.username(), d.email(), d.password(), d.roles());
  }

  @PatchMapping("/usuarios/{id}/acceso")
  public Object access(@PathVariable Long id, @Valid @RequestBody UserAccess d) {
    return users.access(id, d);
  }

  @GetMapping("/auditoria")
  public Object audit(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "") String usuario,
      @RequestParam(defaultValue = "") String tabla) {
    return audit.findByUsuarioContainingIgnoreCaseAndTablaContainingIgnoreCase(
        usuario,
        tabla,
        PageRequest.of(
            Math.max(0, page),
            Math.max(1, Math.min(size, 100)),
            Sort.by(Sort.Direction.DESC, "id")));
  }

  @GetMapping("/test/all")
  public Object publicRoute() {
    return Map.of("message", "Public Content");
  }

  @GetMapping("/test/user")
  public Object userRoute() {
    return Map.of("message", "User Content");
  }

  @GetMapping("/test/admin")
  public Object adminRoute() {
    return Map.of("message", "Admin Content");
  }

  @GetMapping("/test/mod")
  public Object modRoute() {
    return Map.of("message", "Moderator Content");
  }
}
