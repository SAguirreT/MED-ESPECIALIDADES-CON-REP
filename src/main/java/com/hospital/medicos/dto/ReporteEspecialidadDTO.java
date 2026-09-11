package com.hospital.medicos.dto;

public class ReporteEspecialidadDTO {
    private String especialidad;
    private long cantidadMedicos;

    public ReporteEspecialidadDTO(String especialidad, long cantidadMedicos) {
        this.especialidad = especialidad;
        this.cantidadMedicos = cantidadMedicos;
    }

    public String getEspecialidad() { return especialidad; }
    public long getCantidadMedicos() { return cantidadMedicos; }
}