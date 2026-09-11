package com.hospital.medicos.dto;

import java.time.LocalTime;

public class SlotDisponibilidadDTO {
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private boolean disponible;
    private String motivo; // por qué no está disponible, si aplica

    public SlotDisponibilidadDTO(LocalTime horaInicio, LocalTime horaFin, boolean disponible, String motivo) {
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.disponible = disponible;
        this.motivo = motivo;
    }

    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public boolean isDisponible() { return disponible; }
    public String getMotivo() { return motivo; }
}