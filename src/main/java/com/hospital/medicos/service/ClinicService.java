package com.hospital.medicos.service;

import com.hospital.medicos.dto.Requests.*;
import com.hospital.medicos.entity.*;
import com.hospital.medicos.repository.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Transactional use cases. All agenda changes acquire the same DB lock before reading conflicts.
 */
@Service
@Transactional
public class ClinicService {
  public static final ZoneId ZONE = ZoneId.of("America/Lima");
  private final MedicoRepository medicos;
  private final EspecialidadRepository especialidades;
  private final ConsultorioRepository consultorios;
  private final HorarioRepository horarios;
  private final BloqueoHorarioRepository bloqueos;
  private final VacacionPermisoRepository vacaciones;
  private final PacienteRepository pacientes;
  private final CitaRepository citas;
  private final AgendaLockRepository locks;
  private final AuditService audit;

  public ClinicService(
      MedicoRepository m,
      EspecialidadRepository e,
      ConsultorioRepository c,
      HorarioRepository h,
      BloqueoHorarioRepository b,
      VacacionPermisoRepository v,
      PacienteRepository p,
      CitaRepository a,
      AgendaLockRepository l,
      AuditService audit) {
    medicos = m;
    especialidades = e;
    consultorios = c;
    horarios = h;
    bloqueos = b;
    vacaciones = v;
    pacientes = p;
    citas = a;
    locks = l;
    this.audit = audit;
  }

  private static ResponseStatusException bad(String s) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, s);
  }

  private static ResponseStatusException conflict(String s) {
    return new ResponseStatusException(HttpStatus.CONFLICT, s);
  }

  private static <T> T found(Optional<T> value, String name) {
    return value.orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, name + " no encontrado"));
  }

  private static boolean active(Boolean b) {
    return Boolean.TRUE.equals(b);
  }

  private static boolean overlaps(LocalTime a, LocalTime b, LocalTime c, LocalTime d) {
    return a.isBefore(d) && c.isBefore(b);
  }

  private static void range(LocalTime a, LocalTime b) {
    if (a == null
        || b == null
        || !a.isBefore(b)
        || a.getSecond() != 0
        || b.getSecond() != 0
        || a.getNano() != 0
        || b.getNano() != 0)
      throw bad(
          "Usa horas con precisión de minutos; el final debe ser posterior al inicio, dentro del"
              + " mismo día");
  }

  private static boolean future(Cita c) {
    return c.getEstado().equals("CONFIRMADA")
        && !LocalDateTime.of(c.getFecha(), c.getHoraFin()).isBefore(LocalDateTime.now(ZONE));
  }

  private void noAppointments(java.util.function.Predicate<Cita> affected) {
    if (citas.findAll().stream().anyMatch(c -> future(c) && affected.test(c)))
      throw conflict("Hay citas confirmadas afectadas. Reprográmalas o cancélalas primero.");
  }

  @Transactional(readOnly = true)
  public List<Medico> medicos() {
    return medicos.findAll();
  }

  @Transactional(readOnly = true)
  public Medico medico(Long id) {
    return found(medicos.findById(id), "Médico");
  }

  @Transactional(readOnly = true)
  public List<Especialidad> especialidades() {
    return especialidades.findAll();
  }

  @Transactional(readOnly = true)
  public Especialidad especialidad(Long id) {
    return found(especialidades.findById(id), "Especialidad");
  }

  @Transactional(readOnly = true)
  public List<Consultorio> consultorios() {
    return consultorios.findAll();
  }

  @Transactional(readOnly = true)
  public Consultorio consultorio(Long id) {
    return found(consultorios.findById(id), "Consultorio");
  }

  @Transactional(readOnly = true)
  public List<Paciente> pacientes() {
    return pacientes.findAll();
  }

  @Transactional(readOnly = true)
  public Paciente paciente(Long id) {
    return found(pacientes.findById(id), "Paciente");
  }

  @Transactional(readOnly = true)
  public List<Horario> horarios() {
    return horarios.findAll();
  }

  @Transactional(readOnly = true)
  public Horario horario(Long id) {
    return found(horarios.findById(id), "Horario");
  }

  @Transactional(readOnly = true)
  public List<BloqueoHorario> bloqueos() {
    return bloqueos.findAll();
  }

  @Transactional(readOnly = true)
  public List<VacacionPermiso> vacaciones() {
    return vacaciones.findAll();
  }

  public Especialidad saveEspecialidad(Long id, EspecialidadInput d) {
    locks.acquire();
    if (especialidades.existsByNombreIgnoreCaseAndIdNot(d.nombre().trim(), id == null ? 0 : id))
      throw conflict("Ya existe esa especialidad");
    var e = id == null ? new Especialidad() : especialidad(id);
    e.setNombre(d.nombre().trim());
    e.setDescripcion(d.descripcion());
    especialidades.save(e);
    audit.record(
        id == null ? "CREAR" : "ACTUALIZAR",
        "especialidades",
        e.getId(),
        "Datos de especialidad guardados");
    return e;
  }

  public Medico saveMedico(Long id, MedicoInput d) {
    locks.acquire();
    if (medicos.existsByCmpAndIdNot(d.cmp().trim(), id == null ? 0 : id))
      throw conflict("El CMP ya pertenece a otro médico");
    var m = id == null ? new Medico() : medico(id);
    var selected = new ArrayList<Especialidad>();
    for (Long eid : new LinkedHashSet<>(d.especialidadIds())) {
      var e = especialidad(eid);
      if (!active(e.getEstado())) throw bad("La especialidad está inactiva");
      selected.add(e);
    }
    if (id != null)
      noAppointments(
          c ->
              c.getMedico().getId().equals(id)
                  && selected.stream()
                      .noneMatch(e -> e.getId().equals(c.getEspecialidad().getId())));
    m.setNombre(d.nombre().trim());
    m.setApellido(d.apellido().trim());
    m.setCmp(d.cmp().trim());
    m.setTelefono(d.telefono());
    m.setEmail(d.email());
    m.setEspecialidades(selected);
    medicos.save(m);
    audit.record(
        id == null ? "CREAR" : "ACTUALIZAR",
        "medicos",
        m.getId(),
        "Datos y especialidades guardados");
    return m;
  }

  public Medico associate(Long id, List<Long> ids, boolean replace) {
    locks.acquire();
    var m = medico(id);
    var all = new LinkedHashSet<Long>();
    if (!replace) m.getEspecialidades().forEach(e -> all.add(e.getId()));
    all.addAll(ids);
    return saveMedico(
        id,
        new MedicoInput(
            m.getNombre(),
            m.getApellido(),
            m.getCmp(),
            m.getTelefono(),
            m.getEmail(),
            new ArrayList<>(all)));
  }

  public Consultorio saveConsultorio(Long id, ConsultorioInput d) {
    locks.acquire();
    if (consultorios.existsByCodigoAndIdNot(d.codigo().trim(), id == null ? 0 : id))
      throw conflict("El código de consultorio ya existe");
    var c = id == null ? new Consultorio() : consultorio(id);
    c.setCodigo(d.codigo().trim());
    c.setNombre(d.nombre().trim());
    c.setPiso(d.piso());
    c.setArea(d.area());
    if (id == null) c.setEstado("DISPONIBLE");
    consultorios.save(c);
    audit.record(
        id == null ? "CREAR" : "ACTUALIZAR", "consultorios", c.getId(), "Consultorio guardado");
    return c;
  }

  public Paciente savePaciente(Long id, PacienteInput d) {
    locks.acquire();
    if (pacientes.existsByDniAndIdNot(d.dni(), id == null ? 0 : id))
      throw conflict("Ya existe un paciente con ese DNI");
    var p = id == null ? new Paciente() : paciente(id);
    p.setNombre(d.nombre().trim());
    p.setApellido(d.apellido().trim());
    p.setDni(d.dni());
    p.setFechaNacimiento(d.fechaNacimiento());
    p.setTelefono(d.telefono());
    p.setEmail(d.email());
    pacientes.save(p);
    audit.record(
        id == null ? "CREAR" : "ACTUALIZAR", "pacientes", p.getId(), "Datos de paciente guardados");
    return p;
  }

  public Object state(String type, Long id, boolean enabled) {
    locks.acquire();
    Object result;
    switch (type) {
      case "medicos" -> {
        var m = medico(id);
        if (!enabled) noAppointments(c -> c.getMedico().getId().equals(id));
        m.setEstado(enabled);
        result = m;
      }
      case "especialidades" -> {
        var e = especialidad(id);
        if (!enabled) noAppointments(c -> c.getEspecialidad().getId().equals(id));
        e.setEstado(enabled);
        result = e;
      }
      case "consultorios" -> {
        var c = consultorio(id);
        if (!enabled) noAppointments(a -> a.getConsultorio().getId().equals(id));
        c.setEstado(enabled ? "DISPONIBLE" : "INACTIVO");
        result = c;
      }
      case "pacientes" -> {
        var p = paciente(id);
        if (!enabled) noAppointments(c -> c.getPaciente().getId().equals(id));
        p.setEstado(enabled);
        result = p;
      }
      case "horarios" -> {
        var h = horario(id);
        if (!enabled) noAppointments(c -> c.getHorario().getId().equals(id));
        else validateSchedule(h);
        h.setEstado(enabled);
        result = h;
      }
      default -> throw bad("Tipo de registro no permitido");
    }
    audit.record(enabled ? "ACTIVAR" : "DESACTIVAR", type, id, "Cambio de estado");
    return result;
  }

  private void validateSchedule(Horario h) {
    range(h.getHoraInicio(), h.getHoraFin());
    int duration = h.getDuracionCitaMinutos();
    if (duration < 5
        || duration > 240
        || ChronoUnit.MINUTES.between(h.getHoraInicio(), h.getHoraFin()) < duration)
      throw bad("Duración de cita entre 5 y 240 minutos y no mayor que el horario");
    if (!active(h.getMedico().getEstado()) || !"DISPONIBLE".equals(h.getConsultorio().getEstado()))
      throw conflict("Médico o consultorio inactivo");
    for (var other : horarios.findAll())
      if (!Objects.equals(h.getId(), other.getId())
          && active(other.getEstado())
          && other.getDiaSemana() == h.getDiaSemana()
          && overlaps(h.getHoraInicio(), h.getHoraFin(), other.getHoraInicio(), other.getHoraFin())
          && (other.getMedico().getId().equals(h.getMedico().getId())
              || (other.getConsultorio() != null
                  && other.getConsultorio().getId().equals(h.getConsultorio().getId()))))
        throw conflict("El médico o consultorio tiene otro horario superpuesto");
  }

  public Horario saveHorario(Long id, Long medicoId, HorarioInput d) {
    locks.acquire();
    var h = id == null ? new Horario() : horario(id);
    if (id != null) noAppointments(c -> c.getHorario().getId().equals(id));
    h.setMedico(medico(medicoId));
    h.setConsultorio(consultorio(d.consultorioId()));
    h.setDiaSemana(d.diaSemana());
    h.setHoraInicio(d.horaInicio());
    h.setHoraFin(d.horaFin());
    h.setDuracionCitaMinutos(d.duracionCitaMinutos());
    validateSchedule(h);
    horarios.save(h);
    audit.record(
        id == null ? "CREAR" : "ACTUALIZAR", "horarios", h.getId(), "Horario semanal guardado");
    return h;
  }

  public Horario assign(Long id, Long room) {
    locks.acquire();
    var h = horario(id);
    return saveHorario(
        id,
        h.getMedico().getId(),
        new HorarioInput(
            h.getDiaSemana(), h.getHoraInicio(), h.getHoraFin(), h.getDuracionCitaMinutos(), room));
  }

  public BloqueoHorario block(Long medicoId, BloqueoInput d) {
    locks.acquire();
    range(d.horaInicio(), d.horaFin());
    var m = medico(medicoId);
    noAppointments(
        c ->
            c.getMedico().getId().equals(medicoId)
                && c.getFecha().equals(d.fecha())
                && overlaps(c.getHoraInicio(), c.getHoraFin(), d.horaInicio(), d.horaFin()));
    var b = new BloqueoHorario();
    b.setMedico(m);
    b.setFecha(d.fecha());
    b.setHoraInicio(d.horaInicio());
    b.setHoraFin(d.horaFin());
    b.setMotivo(d.motivo());
    b.setEstado("BLOQUEADO");
    bloqueos.save(b);
    audit.record("BLOQUEAR", "bloqueos_horario", b.getId(), "Bloqueo de agenda registrado");
    return b;
  }

  public VacacionPermiso vacation(VacacionInput d) {
    locks.acquire();
    if (d.fechaFin().isBefore(d.fechaInicio()))
      throw bad("La fecha final debe ser igual o posterior a la inicial");
    var m = medico(d.medicoId());
    noAppointments(
        c ->
            c.getMedico().getId().equals(d.medicoId())
                && !c.getFecha().isBefore(d.fechaInicio())
                && !c.getFecha().isAfter(d.fechaFin()));
    var v = new VacacionPermiso();
    v.setMedico(m);
    v.setFechaInicio(d.fechaInicio());
    v.setFechaFin(d.fechaFin());
    v.setMotivo(d.motivo());
    v.setEstado("NO_DISPONIBLE");
    vacaciones.save(v);
    audit.record("CREAR", "vacaciones_permisos", v.getId(), "Ausencia registrada");
    return v;
  }

  public Object cancelAbsence(String type, Long id) {
    locks.acquire();
    Object result;
    if (type.equals("bloqueos")) {
      var b = found(bloqueos.findById(id), "Bloqueo");
      b.setEstado("CANCELADO");
      result = b;
    } else {
      var v = found(vacaciones.findById(id), "Ausencia");
      v.setEstado("CANCELADO");
      result = v;
    }
    audit.record(
        "CANCELAR",
        type.equals("bloqueos") ? "bloqueos_horario" : "vacaciones_permisos",
        id,
        "Agenda liberada");
    return result;
  }

  public record Slot(
      Long horarioId,
      Long consultorioId,
      String consultorio,
      LocalTime horaInicio,
      LocalTime horaFin,
      boolean disponible,
      String motivo) {}

  @Transactional(readOnly = true)
  public List<Slot> availability(Long medicoId, LocalDate date) {
    return availability(medicoId, date, null);
  }

  private List<Slot> availability(Long medicoId, LocalDate date, Long exclude) {
    var m = medico(medicoId);
    var result = new ArrayList<Slot>();
    if (!active(m.getEstado())) return result;
    var day = DiaSemana.values()[date.getDayOfWeek().getValue() - 1];
    var bs = bloqueos.findByMedicoId(medicoId);
    var vs = vacaciones.findByMedicoId(medicoId);
    var appointments = citas.findByFechaBetweenOrderByFechaAscHoraInicioAsc(date, date);
    for (var h : horarios.findByMedicoId(medicoId)) {
      if (!active(h.getEstado())
          || h.getDiaSemana() != day
          || h.getConsultorio() == null
          || !"DISPONIBLE".equals(h.getConsultorio().getEstado())) continue;
      int duration = h.getDuracionCitaMinutos();
      if (duration < 5 || duration > 240) continue;
      long total = ChronoUnit.MINUTES.between(h.getHoraInicio(), h.getHoraFin());
      // Bounded arithmetic avoids LocalTime wrapping around midnight.
      for (long offset = 0; offset + duration <= total; offset += duration) {
        var from = h.getHoraInicio().plusMinutes(offset);
        var to = from.plusMinutes(duration);
        String reason = null;
        if (!LocalDateTime.of(date, from).isAfter(LocalDateTime.now(ZONE)))
          reason = "Horario pasado";
        for (var v : vs)
          if ("NO_DISPONIBLE".equals(v.getEstado())
              && !date.isBefore(v.getFechaInicio())
              && !date.isAfter(v.getFechaFin())) reason = "Vacaciones o permiso";
        for (var b : bs)
          if ("BLOQUEADO".equals(b.getEstado())
              && b.getFecha().equals(date)
              && overlaps(from, to, b.getHoraInicio(), b.getHoraFin()))
            reason = "Bloqueo de agenda";
        for (var c : appointments)
          if (!Objects.equals(c.getId(), exclude)
              && !"CANCELADA".equals(c.getEstado())
              && (c.getMedico().getId().equals(medicoId)
                  || c.getConsultorio().getId().equals(h.getConsultorio().getId()))
              && overlaps(from, to, c.getHoraInicio(), c.getHoraFin())) reason = "Cita reservada";
        result.add(
            new Slot(
                h.getId(),
                h.getConsultorio().getId(),
                h.getConsultorio().getCodigo(),
                from,
                to,
                reason == null,
                reason));
      }
    }
    result.sort(Comparator.comparing(Slot::horaInicio));
    return result;
  }

  @Transactional(readOnly = true)
  public List<Cita> citas(LocalDate from, LocalDate to) {
    if (to.isBefore(from) || ChronoUnit.DAYS.between(from, to) > 366)
      throw bad("Consulta un rango de hasta 366 días");
    return citas.findByFechaBetweenOrderByFechaAscHoraInicioAsc(from, to);
  }

  public Cita saveCita(Long id, CitaInput d) {
    locks.acquire();
    var p = paciente(d.pacienteId());
    var m = medico(d.medicoId());
    var e = especialidad(d.especialidadId());
    if (!active(p.getEstado()) || !active(m.getEstado()) || !active(e.getEstado()))
      throw conflict("Paciente, médico o especialidad inactivo");
    if (m.getEspecialidades().stream().noneMatch(x -> x.getId().equals(e.getId())))
      throw bad("El médico no atiende esta especialidad");
    var c = id == null ? new Cita() : found(citas.findById(id), "Cita");
    if (id != null && !"CONFIRMADA".equals(c.getEstado()))
      throw conflict("Solo se pueden reprogramar citas confirmadas");
    var slot =
        availability(m.getId(), d.fecha(), id).stream()
            .filter(
                s ->
                    s.horarioId().equals(d.horarioId())
                        && s.horaInicio().equals(d.horaInicio())
                        && s.disponible())
            .findFirst()
            .orElseThrow(() -> conflict("La franja ya no está disponible. Actualiza la agenda."));
    for (var other : citas.findByFechaBetweenOrderByFechaAscHoraInicioAsc(d.fecha(), d.fecha()))
      if (!Objects.equals(other.getId(), id)
          && other.getPaciente().getId().equals(p.getId())
          && !"CANCELADA".equals(other.getEstado())
          && overlaps(slot.horaInicio(), slot.horaFin(), other.getHoraInicio(), other.getHoraFin()))
        throw conflict("El paciente tiene otra cita en ese intervalo");
    c.setPaciente(p);
    c.setMedico(m);
    c.setEspecialidad(e);
    c.setHorario(horario(slot.horarioId()));
    c.setConsultorio(consultorio(slot.consultorioId()));
    c.setFecha(d.fecha());
    c.setHoraInicio(slot.horaInicio());
    c.setHoraFin(slot.horaFin());
    c.setMotivo(d.motivo());
    c.setEstado("CONFIRMADA");
    citas.save(c);
    audit.record(
        id == null ? "RESERVAR" : "REPROGRAMAR",
        "citas",
        c.getId(),
        "Cita para " + d.fecha() + " " + d.horaInicio());
    return c;
  }

  public Cita cancelCita(Long id, String reason) {
    locks.acquire();
    var c = found(citas.findById(id), "Cita");
    if (!"CONFIRMADA".equals(c.getEstado()))
      throw conflict("Solo se pueden cancelar citas confirmadas");
    c.setEstado("CANCELADA");
    c.setMotivoCancelacion(reason);
    audit.record("CANCELAR", "citas", id, "Cita cancelada; motivo conservado en la cita");
    return c;
  }

  public Cita completeCita(Long id) {
    locks.acquire();
    var c = found(citas.findById(id), "Cita");
    if (!"CONFIRMADA".equals(c.getEstado()))
      throw conflict("Solo se pueden atender citas confirmadas");
    if (LocalDateTime.of(c.getFecha(), c.getHoraInicio()).isAfter(LocalDateTime.now(ZONE)))
      throw conflict("La cita todavía no ha comenzado");
    c.setEstado("ATENDIDA");
    audit.record("ATENDER", "citas", id, "Atención registrada");
    return c;
  }

  @Transactional(readOnly = true)
  public List<Map<String, Object>> report() {
    return especialidades.findAll().stream()
        .map(
            e -> {
              Map<String, Object> row = new LinkedHashMap<>();
              row.put("especialidad", e.getNombre());
              row.put("activa", e.getEstado());
              row.put(
                  "cantidadMedicos",
                  medicos.findAll().stream()
                      .filter(
                          m ->
                              active(m.getEstado())
                                  && m.getEspecialidades().stream()
                                      .anyMatch(x -> x.getId().equals(e.getId())))
                      .count());
              return row;
            })
        .toList();
  }

  @Transactional(readOnly = true)
  public Map<String, Object> dashboard() {
    var today = LocalDate.now(ZONE);
    var day = citas(today, today);
    return Map.of(
        "pacientes",
        pacientes.count(),
        "medicosActivos",
        medicos.findAll().stream().filter(m -> active(m.getEstado())).count(),
        "citasHoy",
        day.size(),
        "pendientesHoy",
        day.stream().filter(c -> c.getEstado().equals("CONFIRMADA")).count(),
        "agenda",
        day,
        "especialidades",
        report());
  }
}
