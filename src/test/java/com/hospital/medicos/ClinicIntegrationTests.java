package com.hospital.medicos;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.hospital.medicos.dto.Requests.*;
import com.hospital.medicos.entity.*;
import com.hospital.medicos.repository.*;
import com.hospital.medicos.security.JwtUtils;
import com.hospital.medicos.service.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClinicIntegrationTests {
  @Autowired MockMvc mvc;
  @Autowired ClinicService clinic;
  @Autowired UserService accounts;
  @Autowired UserRepository users;
  @Autowired JwtUtils jwt;
  @Autowired CitaRepository citas;
  @Autowired AuditoriaRepository audits;
  @Autowired HorarioRepository horarios;

  String admin() {
    return jwt.generateJwtToken(users.findByEmail("admin@test.local").orElseThrow());
  }

  String reader() {
    var email = "reader" + UUID.randomUUID() + "@test.local";
    return jwt.generateJwtToken(accounts.create(email, email, "ReaderTest2026!", Set.of("USER")));
  }

  record Fixture(
      Medico medico,
      Especialidad especialidad,
      Paciente paciente,
      Consultorio consultorio,
      Horario horario,
      LocalDate fecha) {}

  Fixture fixture() {
    String n = UUID.randomUUID().toString().substring(0, 8);
    var date = LocalDate.now(ClinicService.ZONE).plusDays(5);
    var e = clinic.saveEspecialidad(null, new EspecialidadInput("Especialidad " + n, "Prueba"));
    var m =
        clinic.saveMedico(null, new MedicoInput("Ana", "Prueba", n, "", "", List.of(e.getId())));
    var room = clinic.saveConsultorio(null, new ConsultorioInput(n, "Sala", "1", "Pruebas"));
    var p =
        clinic.savePaciente(
            null,
            new PacienteInput(
                "Luis",
                "Prueba",
                String.format("%08d", Math.abs(n.hashCode()) % 100000000),
                LocalDate.of(1990, 1, 1),
                "",
                ""));
    var h =
        clinic.saveHorario(
            null,
            m.getId(),
            new HorarioInput(
                DiaSemana.values()[date.getDayOfWeek().getValue() - 1],
                LocalTime.of(9, 0),
                LocalTime.of(11, 0),
                30,
                room.getId()));
    return new Fixture(m, e, p, room, h, date);
  }

  CitaInput request(Fixture f) {
    return new CitaInput(
        f.paciente.getId(),
        f.medico.getId(),
        f.especialidad.getId(),
        f.horario.getId(),
        f.fecha,
        LocalTime.of(9, 0),
        "Control");
  }

  @Test
  void sixSecurityCasesAndLogin() throws Exception {
    mvc.perform(get("/api/test/all")).andExpect(status().isOk());
    mvc.perform(get("/api/test/user")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/test/user").header("Authorization", "Bearer invalid.token.signature"))
        .andExpect(status().isUnauthorized());
    var token = reader();
    mvc.perform(get("/api/test/user").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
    mvc.perform(get("/api/test/admin").header("Authorization", "Bearer " + token))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/test/admin").header("Authorization", "Bearer " + admin()))
        .andExpect(status().isOk());
    mvc.perform(
            post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@test.local\",\"password\":\"AdminTest2026!\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").isString());
    mvc.perform(
            post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@test.local\",\"password\":\"wrong\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void publicSignupCannotChooseAdmin() throws Exception {
    String email = "signup" + UUID.randomUUID() + "@test.local";
    mvc.perform(
            post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"username\":\""
                        + email
                        + "\",\"email\":\""
                        + email
                        + "\",\"password\":\"SignupTest2026!\",\"roles\":[\"ADMIN\"]}"))
        .andExpect(status().isCreated());
    var user = users.findByEmail(email).orElseThrow();
    assertEquals(
        Set.of("USER"),
        user.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet()));
    assertTrue(user.getPassword().startsWith("$2"));
    assertFalse(user.getActivo());
    mvc.perform(
            post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"SignupTest2026!\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void roleRestrictionAndLogout() throws Exception {
    String token = reader();
    mvc.perform(
            post("/api/pacientes")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isForbidden());
    mvc.perform(post("/api/auth/signout").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk());
    mvc.perform(get("/api/test/user").header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void disabledUserAndExpiredJwtRejected() throws Exception {
    var u =
        accounts.create(
            "expire" + UUID.randomUUID(),
            "expire" + UUID.randomUUID() + "@test.local",
            "ExpiredTest2026!",
            Set.of("USER"));
    var expired =
        new JwtUtils("clave-exclusiva-pruebas-automatizadas-2026-123456", -1000)
            .generateJwtToken(u);
    mvc.perform(get("/api/test/user").header("Authorization", "Bearer " + expired))
        .andExpect(status().isUnauthorized());
    var token = jwt.generateJwtToken(u);
    accounts.access(u.getId(), new UserAccess(false, Set.of("USER")));
    mvc.perform(get("/api/test/user").header("Authorization", "Bearer " + token))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void zeroDurationRejectedAndDoctorNotRequiredInBody() throws Exception {
    var f = fixture();
    String body =
        "{\"diaSemana\":\"LUNES\",\"horaInicio\":\"13:00\",\"horaFin\":\"14:00\",\"duracionCitaMinutos\":0,\"consultorioId\":"
            + f.consultorio.getId()
            + "}";
    mvc.perform(
            post("/api/horarios/medico/" + f.medico.getId())
                .header("Authorization", "Bearer " + admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/api/horarios/medico/" + f.medico.getId())
                .header("Authorization", "Bearer " + admin())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body.replace("Minutos\":0", "Minutos\":30")))
        .andExpect(status().isCreated());
  }

  @Test
  void boundedMidnightAndLegacyZeroDuration() {
    var f = fixture();
    var h =
        clinic.saveHorario(
            f.horario.getId(),
            f.medico.getId(),
            new HorarioInput(
                f.horario.getDiaSemana(),
                LocalTime.of(23, 0),
                LocalTime.of(23, 59),
                30,
                f.consultorio.getId()));
    assertTimeoutPreemptively(
        Duration.ofSeconds(3),
        () -> assertEquals(1, clinic.availability(f.medico.getId(), f.fecha).size()));
    h.setDuracionCitaMinutos(0);
    horarios.save(h);
    assertTimeoutPreemptively(
        Duration.ofSeconds(3),
        () -> assertTrue(clinic.availability(f.medico.getId(), f.fecha).isEmpty()));
  }

  @Test
  void reservationsBlocksAndAudit() {
    var f = fixture();
    long before = audits.count();
    var c = clinic.saveCita(null, request(f));
    assertFalse(clinic.availability(f.medico.getId(), f.fecha).get(0).disponible());
    assertThrows(ResponseStatusException.class, () -> clinic.saveCita(null, request(f)));
    assertThrows(
        ResponseStatusException.class, () -> clinic.state("medicos", f.medico.getId(), false));
    assertThrows(
        ResponseStatusException.class,
        () ->
            clinic.block(
                f.medico.getId(),
                new BloqueoInput(f.fecha, LocalTime.of(9, 0), LocalTime.of(10, 0), "Reunión")));
    clinic.cancelCita(c.getId(), "Solicitud del paciente");
    assertTrue(clinic.availability(f.medico.getId(), f.fecha).get(0).disponible());
    var b =
        clinic.block(
            f.medico.getId(),
            new BloqueoInput(f.fecha, LocalTime.of(9, 0), LocalTime.of(10, 0), "Reunión"));
    assertFalse(clinic.availability(f.medico.getId(), f.fecha).get(0).disponible());
    clinic.cancelAbsence("bloqueos", b.getId());
    assertTrue(clinic.availability(f.medico.getId(), f.fecha).get(0).disponible());
    assertTrue(audits.count() > before);
  }

  @Test
  void vacationsAndInactiveDoctor() {
    var f = fixture();
    var v = clinic.vacation(new VacacionInput(f.medico.getId(), f.fecha, f.fecha, "Permiso"));
    assertTrue(
        clinic.availability(f.medico.getId(), f.fecha).stream()
            .noneMatch(ClinicService.Slot::disponible));
    clinic.cancelAbsence("vacaciones", v.getId());
    assertTrue(clinic.availability(f.medico.getId(), f.fecha).get(0).disponible());
    clinic.state("medicos", f.medico.getId(), false);
    assertTrue(clinic.availability(f.medico.getId(), f.fecha).isEmpty());
  }

  @Test
  void overlapAndDuplicateCmpRejected() {
    var f = fixture();
    assertThrows(
        ResponseStatusException.class,
        () ->
            clinic.saveHorario(
                null,
                f.medico.getId(),
                new HorarioInput(
                    f.horario.getDiaSemana(),
                    LocalTime.of(10, 0),
                    LocalTime.of(12, 0),
                    30,
                    f.consultorio.getId())));
    assertThrows(
        ResponseStatusException.class,
        () ->
            clinic.saveMedico(
                null, new MedicoInput("Otro", "Médico", f.medico.getCmp(), "", "", List.of())));
  }

  @Test
  void concurrentReservationsHaveExactlyOneWinner() throws Exception {
    var f = fixture();
    var pool = Executors.newFixedThreadPool(2);
    var start = new CountDownLatch(1);
    Callable<Boolean> task =
        () -> {
          start.await();
          try {
            clinic.saveCita(null, request(f));
            return true;
          } catch (ResponseStatusException e) {
            assertEquals(409, e.getStatusCode().value());
            return false;
          }
        };
    try {
      var a = pool.submit(task);
      var b = pool.submit(task);
      start.countDown();
      assertNotEquals(a.get(10, TimeUnit.SECONDS), b.get(10, TimeUnit.SECONDS));
    } finally {
      pool.shutdownNow();
    }
  }

  @Test
  void lastAdminProtected() {
    var u = users.findByEmail("admin@test.local").orElseThrow();
    assertThrows(
        ResponseStatusException.class,
        () -> accounts.access(u.getId(), new UserAccess(false, Set.of("USER"))));
  }

  @Test
  void patientCannotAttendTwoDoctorsAtOnce() {
    var a = fixture();
    var b = fixture();
    clinic.saveCita(null, request(a));
    var other =
        new CitaInput(
            a.paciente.getId(),
            b.medico.getId(),
            b.especialidad.getId(),
            b.horario.getId(),
            b.fecha,
            LocalTime.of(9, 0),
            "Segunda consulta");
    assertThrows(ResponseStatusException.class, () -> clinic.saveCita(null, other));
  }

  @Test
  void scheduleUpdateAndRoomAssignmentCannotBypassConflicts() {
    var a = fixture();
    var b = fixture();
    assertThrows(
        ResponseStatusException.class,
        () -> clinic.assign(b.horario.getId(), a.consultorio.getId()));
    var late =
        clinic.saveHorario(
            null,
            a.medico.getId(),
            new HorarioInput(
                a.horario.getDiaSemana(),
                LocalTime.of(13, 0),
                LocalTime.of(14, 0),
                30,
                a.consultorio.getId()));
    assertThrows(
        ResponseStatusException.class,
        () ->
            clinic.saveHorario(
                late.getId(),
                a.medico.getId(),
                new HorarioInput(
                    a.horario.getDiaSemana(),
                    LocalTime.of(10, 0),
                    LocalTime.of(12, 0),
                    30,
                    a.consultorio.getId())));
    clinic.saveCita(null, request(a));
    assertThrows(
        ResponseStatusException.class,
        () ->
            clinic.saveHorario(
                a.horario.getId(),
                a.medico.getId(),
                new HorarioInput(
                    a.horario.getDiaSemana(),
                    LocalTime.of(13, 0),
                    LocalTime.of(14, 0),
                    30,
                    a.consultorio.getId())));
  }

  @Test
  void rescheduleFreesOriginalSlotAndFutureCannotBeAttended() {
    var f = fixture();
    var c = clinic.saveCita(null, request(f));
    assertThrows(ResponseStatusException.class, () -> clinic.completeCita(c.getId()));
    clinic.saveCita(
        c.getId(),
        new CitaInput(
            f.paciente.getId(),
            f.medico.getId(),
            f.especialidad.getId(),
            f.horario.getId(),
            f.fecha,
            LocalTime.of(10, 0),
            "Cambio"));
    assertTrue(clinic.availability(f.medico.getId(), f.fecha).get(0).disponible());
    assertFalse(clinic.availability(f.medico.getId(), f.fecha).get(2).disponible());
  }

  @Test
  void missingDoctorAndWrongSpecialtyRejected() throws Exception {
    mvc.perform(
            get("/api/disponibilidad/medico/999999?fecha=2026-12-01")
                .header("Authorization", "Bearer " + admin()))
        .andExpect(status().isNotFound());
    var a = fixture();
    var b = fixture();
    assertThrows(
        ResponseStatusException.class,
        () ->
            clinic.saveCita(
                null,
                new CitaInput(
                    a.paciente.getId(),
                    a.medico.getId(),
                    b.especialidad.getId(),
                    a.horario.getId(),
                    a.fecha,
                    LocalTime.of(9, 0),
                    "Consulta")));
  }

  @Test
  void secretsNeverSerialized() throws Exception {
    mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + admin()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].password").doesNotExist())
        .andExpect(jsonPath("$[0].tokenVersion").doesNotExist());
  }

  @Test
  void concurrentRoomAssignmentsHaveOneWinner() throws Exception {
    var a = fixture();
    var b = fixture();
    var room =
        clinic.saveConsultorio(
            null,
            new ConsultorioInput(
                "C-" + UUID.randomUUID().toString().substring(0, 8),
                "Sala compartida",
                "1",
                "Pruebas"));
    var pool = Executors.newFixedThreadPool(2);
    var start = new CountDownLatch(1);
    java.util.function.Function<Long, Callable<Boolean>> task =
        id ->
            () -> {
              start.await();
              try {
                clinic.assign(id, room.getId());
                return true;
              } catch (ResponseStatusException e) {
                assertEquals(409, e.getStatusCode().value());
                return false;
              }
            };
    try {
      var first = pool.submit(task.apply(a.horario.getId()));
      var second = pool.submit(task.apply(b.horario.getId()));
      start.countDown();
      assertNotEquals(first.get(10, TimeUnit.SECONDS), second.get(10, TimeUnit.SECONDS));
    } finally {
      pool.shutdownNow();
    }
  }
}
