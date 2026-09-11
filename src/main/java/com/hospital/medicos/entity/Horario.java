package com.hospital.medicos.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import java.util.Objects;

@Entity
@Table(name = "horarios")
public class Horario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consultorio_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Consultorio consultorio; // RF-MED-16, opcional al crear el horario

    public Consultorio getConsultorio() { return consultorio; }
    public void setConsultorio(Consultorio consultorio) { this.consultorio = consultorio; }

    @NotNull(message = "El médico es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    @JsonIgnoreProperties({"especialidades", "hibernateLazyInitializer", "handler"})
    private Medico medico;

    @NotNull(message = "El día de la semana es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private DiaSemana diaSemana;

    @NotNull(message = "La hora de inicio es obligatoria")
    @Column(nullable = false)
    private LocalTime horaInicio;

    @NotNull(message = "La hora de fin es obligatoria")
    @Column(nullable = false)
    private LocalTime horaFin;

    @NotNull(message = "La duración de la cita es obligatoria")
    @Column(nullable = false)
    private Integer duracionCitaMinutos; // ej. 30

    @Column(nullable = false)
    private Boolean estado = true; // activo/inactivo

    public Horario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }

    public DiaSemana getDiaSemana() { return diaSemana; }
    public void setDiaSemana(DiaSemana diaSemana) { this.diaSemana = diaSemana; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public Integer getDuracionCitaMinutos() { return duracionCitaMinutos; }
    public void setDuracionCitaMinutos(Integer duracionCitaMinutos) { this.duracionCitaMinutos = duracionCitaMinutos; }

    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Horario)) return false;
        Horario horario = (Horario) o;
        return id != null && id.equals(horario.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}