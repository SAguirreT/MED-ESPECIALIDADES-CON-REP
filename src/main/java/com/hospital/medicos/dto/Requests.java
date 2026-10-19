package com.hospital.medicos.dto;

import com.hospital.medicos.entity.DiaSemana;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.*;

/** API input contracts deliberately exclude IDs and persistence-only fields. */
public final class Requests {
  public record MedicoInput(
      @NotBlank @Size(max = 100) String nombre,
      @NotBlank @Size(max = 100) String apellido,
      @NotBlank @Size(max = 20) String cmp,
      @Size(max = 20) String telefono,
      @Email @Size(max = 100) String email,
      @NotNull List<@NotNull Long> especialidadIds) {}

  public record EspecialidadInput(
      @NotBlank @Size(max = 100) String nombre, @Size(max = 255) String descripcion) {}

  public record ConsultorioInput(
      @NotBlank @Size(max = 20) String codigo,
      @NotBlank @Size(max = 100) String nombre,
      @Size(max = 20) String piso,
      @Size(max = 100) String area) {}

  public record PacienteInput(
      @NotBlank @Size(max = 100) String nombre,
      @NotBlank @Size(max = 100) String apellido,
      @NotNull @Pattern(regexp = "[0-9]{8}") String dni,
      @NotNull @PastOrPresent LocalDate fechaNacimiento,
      @Size(max = 20) String telefono,
      @Email @Size(max = 100) String email) {}

  public record HorarioInput(
      @NotNull DiaSemana diaSemana,
      @NotNull LocalTime horaInicio,
      @NotNull LocalTime horaFin,
      @NotNull @Min(5) @Max(240) Integer duracionCitaMinutos,
      @NotNull Long consultorioId) {}

  public record BloqueoInput(
      @NotNull LocalDate fecha,
      @NotNull LocalTime horaInicio,
      @NotNull LocalTime horaFin,
      @NotBlank @Size(max = 255) String motivo) {}

  public record VacacionInput(
      @NotNull Long medicoId,
      @NotNull LocalDate fechaInicio,
      @NotNull LocalDate fechaFin,
      @NotBlank @Size(max = 255) String motivo) {}

  public record CitaInput(
      @NotNull Long pacienteId,
      @NotNull Long medicoId,
      @NotNull Long especialidadId,
      @NotNull Long horarioId,
      @NotNull LocalDate fecha,
      @NotNull LocalTime horaInicio,
      @NotBlank @Size(max = 255) String motivo) {}

  public record Cancelacion(@NotBlank @Size(max = 255) String motivo) {}

  public record Login(@NotBlank @Email String email, @NotBlank String password) {}

  public record Signup(
      @NotBlank @Size(min = 3, max = 60) String username,
      @NotBlank @Email @Size(max = 100) String email,
      @NotBlank @Size(min = 10, max = 64) String password) {}

  public record UserInput(
      @NotBlank @Size(min = 3, max = 60) String username,
      @NotBlank @Email @Size(max = 100) String email,
      @NotBlank @Size(min = 10, max = 64) String password,
      @NotEmpty Set<@Pattern(regexp = "ADMIN|MODERATOR|USER") String> roles) {}

  public record UserAccess(
      @NotNull Boolean activo,
      @NotEmpty Set<@Pattern(regexp = "ADMIN|MODERATOR|USER") String> roles) {}

  public record PasswordChange(
      @NotBlank String actual, @NotBlank @Size(min = 10, max = 64) String nueva) {}
}
