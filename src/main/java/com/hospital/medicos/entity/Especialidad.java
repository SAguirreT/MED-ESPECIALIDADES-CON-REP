package com.hospital.medicos.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "especialidades")
public class Especialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la especialidad es obligatorio")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false)
    private Boolean estado = true; // RF-MED-09

    @ManyToMany(mappedBy = "especialidades")
    @JsonIgnoreProperties("especialidades") // Evita bucle infinito en la respuesta JSON
    private List<Medico> medicos = new ArrayList<>();

    public Especialidad() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Boolean getEstado() { return estado; }
    public void setEstado(Boolean estado) { this.estado = estado; }

    public List<Medico> getMedicos() { return medicos; }
    public void setMedicos(List<Medico> medicos) { this.medicos = medicos; }

    // equals/hashCode basados en ID: necesario para que contains() funcione
    // de forma confiable en colecciones @ManyToMany entre distintas sesiones
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Especialidad)) return false;
        Especialidad that = (Especialidad) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}