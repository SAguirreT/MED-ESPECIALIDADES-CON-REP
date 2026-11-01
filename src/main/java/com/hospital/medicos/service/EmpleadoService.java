package com.hospital.medicos.service;

import com.hospital.medicos.entity.Empleado;
import com.hospital.medicos.repository.EmpleadoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;

    public EmpleadoService(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    @Transactional
    public Empleado registrar(Empleado empleado) {
        empleado.setId(null);
        return empleadoRepository.save(empleado);
    }

    @Transactional(readOnly = true)
    public List<Empleado> listar() {
        return empleadoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Empleado buscar(Long id) {
        return empleadoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Empleado no encontrado con ID: " + id));
    }

    @Transactional
    public Empleado actualizar(Long id, Empleado datos) {
        Empleado empleado = buscar(id);
        empleado.setNombres(datos.getNombres());
        empleado.setApellidos(datos.getApellidos());
        empleado.setEmail(datos.getEmail());
        empleado.setSueldo(datos.getSueldo());
        return empleadoRepository.save(empleado);
    }

    @Transactional
    public void eliminar(Long id) {
        empleadoRepository.delete(buscar(id));
    }
}
