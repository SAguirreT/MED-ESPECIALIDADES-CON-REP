package com.hospital.medicos.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "vacaciones_permisos")
public class VacacionPermiso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medico_id", nullable = false)
    @JsonIgnoreProperties({"especialidades", "hibernateLazyInitializer", "handler"})
    private Medico medico;

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaInicio;

    @NotNull
    @Column(nullable = false)
    private LocalDate fechaFin;

    @Column(length = 255)
    private String motivo;

    @Column(nullable = false, length = 20)
    private String estado = "NO_DISPONIBLE";

    public VacacionPermiso() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VacacionPermiso)) return false;
        return id != null && id.equals(((VacacionPermiso) o).id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}