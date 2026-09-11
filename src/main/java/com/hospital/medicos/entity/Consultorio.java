package com.hospital.medicos.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "consultorios")
public class Consultorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El código es obligatorio")
    @Column(nullable = false, length = 20, unique = true)
    private String codigo; // ej. C-205

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 20)
    private String piso;

    @Column(length = 100)
    private String area;

    @Column(nullable = false, length = 20)
    private String estado = "DISPONIBLE"; // DISPONIBLE / OCUPADO / INACTIVO

    public Consultorio() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getPiso() { return piso; }
    public void setPiso(String piso) { this.piso = piso; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Consultorio)) return false;
        Consultorio that = (Consultorio) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}