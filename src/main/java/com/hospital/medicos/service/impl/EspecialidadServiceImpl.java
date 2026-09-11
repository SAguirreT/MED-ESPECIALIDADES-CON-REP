package com.hospital.medicos.service.impl;

import com.hospital.medicos.entity.Especialidad;
import com.hospital.medicos.repository.EspecialidadRepository;
import com.hospital.medicos.service.EspecialidadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class EspecialidadServiceImpl implements EspecialidadService {

    private final EspecialidadRepository especialidadRepository;

    @Autowired
    public EspecialidadServiceImpl(EspecialidadRepository especialidadRepository) {
        this.especialidadRepository = especialidadRepository;
    }

    @Override
    @Transactional
    public Especialidad registrarEspecialidad(Especialidad especialidad) {
        especialidad.setEstado(true);
        return especialidadRepository.save(especialidad);
    }

    @Override
    @Transactional
    public Especialidad actualizarEspecialidad(Long id, Especialidad detalles) {
        Especialidad especialidad = obtenerPorId(id);
        especialidad.setNombre(detalles.getNombre());
        especialidad.setDescripcion(detalles.getDescripcion());
        return especialidadRepository.save(especialidad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Especialidad> obtenerTodas() {
        return especialidadRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Especialidad obtenerPorId(Long id) {
        return especialidadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Especialidad no encontrada con ID: " + id));
    }

    @Override
    @Transactional
    public Especialidad cambiarEstado(Long id, Boolean estado) {
        Especialidad especialidad = obtenerPorId(id);
        especialidad.setEstado(estado);
        return especialidadRepository.save(especialidad);
    }
}