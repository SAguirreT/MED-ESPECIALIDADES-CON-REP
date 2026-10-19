package com.hospital.medicos.config;

import com.hospital.medicos.dto.Requests.*;
import com.hospital.medicos.entity.*;
import com.hospital.medicos.repository.*;
import com.hospital.medicos.service.*;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {
  private final RoleRepository roles;
  private final UserRepository users;
  private final UserService accounts;
  private final AgendaLockRepository locks;
  private final ClinicService clinic;
  private final CitaRepository citas;
  private final boolean demo;
  private final String email, password;

  public DataInitializer(
      RoleRepository roles,
      UserRepository users,
      UserService accounts,
      AgendaLockRepository locks,
      ClinicService clinic,
      CitaRepository citas,
      @Value("${app.demo}") boolean demo,
      @Value("${app.bootstrap.email}") String email,
      @Value("${app.bootstrap.password}") String password) {
    this.roles = roles;
    this.users = users;
    this.accounts = accounts;
    this.locks = locks;
    this.clinic = clinic;
    this.citas = citas;
    this.demo = demo;
    this.email = email;
    this.password = password;
  }

  @Override
  @Transactional
  public void run(String... args) {
    for (String name : List.of("ADMIN", "MODERATOR", "USER"))
      if (roles.findByName(name).isEmpty()) {
        var r = new Role();
        r.setName(name);
        roles.save(r);
      }
    if (!locks.existsById(1L)) locks.saveAndFlush(new AgendaLock(1L));
    if (demo) {
      if (!users.existsByEmail("admin@aurora.demo"))
        accounts.create("Administración", "admin@aurora.demo", "AuroraAdmin2026!", Set.of("ADMIN"));
      if (!users.existsByEmail("recepcion@aurora.demo"))
        accounts.create(
            "Recepción", "recepcion@aurora.demo", "AuroraRecepcion2026!", Set.of("MODERATOR"));
      if (!users.existsByEmail("consulta@aurora.demo"))
        accounts.create("Consulta", "consulta@aurora.demo", "AuroraConsulta2026!", Set.of("USER"));
      if (clinic.medicos().isEmpty() && clinic.especialidades().isEmpty()) seed();
    } else if (users.count() == 0) {
      if (email.isBlank() || password.length() < 10)
        throw new IllegalStateException(
            "Configura ADMIN_EMAIL y ADMIN_PASSWORD (mínimo 10 caracteres) para el primer inicio, o"
                + " activa el perfil demo.");
      accounts.create("Administrador", email, password, Set.of("ADMIN"));
    }
  }

  private void seed() {
    String[] names = {"Cardiología", "Pediatría", "Medicina general", "Dermatología"};
    String[] desc = {
      "Prevención y cuidado cardiovascular",
      "Atención integral de niños y adolescentes",
      "Evaluación y seguimiento de la salud",
      "Cuidado de la piel"
    };
    String[] first = {"Valeria", "Diego", "Lucía", "Andrés"},
        last = {"Mendoza", "Salazar", "Torres", "Ríos"};
    var doctors = new ArrayList<Medico>();
    var specialties = new ArrayList<Especialidad>();
    var schedules = new ArrayList<Horario>();
    for (int i = 0; i < 4; i++) {
      var e = clinic.saveEspecialidad(null, new EspecialidadInput(names[i], desc[i]));
      specialties.add(e);
      var m =
          clinic.saveMedico(
              null,
              new MedicoInput(
                  first[i],
                  last[i],
                  "DEMO-" + (100 + i),
                  "90000000" + i,
                  "medico" + i + "@example.com",
                  List.of(e.getId())));
      doctors.add(m);
      var room =
          clinic.saveConsultorio(
              null,
              new ConsultorioInput("C-10" + (i + 1), "Consultorio " + (i + 1), "1", names[i]));
      for (var day : DiaSemana.values())
        schedules.add(
            clinic.saveHorario(
                null,
                m.getId(),
                new HorarioInput(day, LocalTime.of(8, 0), LocalTime.of(18, 0), 30, room.getId())));
    }
    String[] pn = {"Mariana", "Jorge", "Camila", "Roberto", "Sofía", "Carlos", "Elena", "Gabriel"},
        pl = {"Vargas", "Flores", "Castro", "Silva", "Ramos", "Vega", "Díaz", "León"};
    var patients = new ArrayList<Paciente>();
    for (int i = 0; i < pn.length; i++)
      patients.add(
          clinic.savePaciente(
              null,
              new PacienteInput(
                  pn[i],
                  pl[i],
                  "0000000" + i,
                  LocalDate.of(1985 + i, 3, 12),
                  "91000000" + i,
                  "paciente" + i + "@example.com")));
    LocalDate today = LocalDate.now(ClinicService.ZONE);
    // Deterministic demonstration data, explicitly fictional. Historical appointments are seeded
    // directly.
    for (int d = 0; d < 3; d++)
      for (int i = 0; i < 6; i++) {
        var m = doctors.get(i % 4);
        var date = today.plusDays(d);
        var day = DiaSemana.values()[date.getDayOfWeek().getValue() - 1];
        var h =
            schedules.stream()
                .filter(x -> x.getMedico().getId().equals(m.getId()) && x.getDiaSemana() == day)
                .findFirst()
                .orElseThrow();
        var c = new Cita();
        c.setMedico(m);
        c.setPaciente(patients.get(i));
        c.setEspecialidad(specialties.get(i % 4));
        c.setHorario(h);
        c.setConsultorio(h.getConsultorio());
        c.setFecha(date);
        c.setHoraInicio(LocalTime.of(9 + i, 0));
        c.setHoraFin(LocalTime.of(9 + i, 30));
        c.setMotivo("Control de salud — caso ficticio");
        c.setEstado(
            LocalDateTime.of(date, c.getHoraFin()).isBefore(LocalDateTime.now(ClinicService.ZONE))
                ? "ATENDIDA"
                : "CONFIRMADA");
        citas.save(c);
      }
  }
}
