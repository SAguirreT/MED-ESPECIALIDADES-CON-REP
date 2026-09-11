package com.hospital.medicos.service.impl;

import com.hospital.medicos.entity.Especialidad;
import com.hospital.medicos.entity.Medico;
import com.hospital.medicos.repository.EspecialidadRepository;
import com.hospital.medicos.repository.MedicoRepository;
import com.hospital.medicos.service.MedicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MedicoServiceImpl implements MedicoService {

    private final MedicoRepository medicoRepository;
    private final EspecialidadRepository especialidadRepository;

    @Autowired
    public MedicoServiceImpl(MedicoRepository medicoRepository, EspecialidadRepository especialidadRepository) {
        this.medicoRepository = medicoRepository;
        this.especialidadRepository = especialidadRepository;
    }

    @Override
    @Transactional
    public Medico registrarMedico(Medico medico) {
        if (medicoRepository.existsByCmp(medico.getCmp())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un médico con el CMP: " + medico.getCmp());
        }
        medico.setEstado(true);
        return medicoRepository.save(medico);
    }

    @Override
    @Transactional
    public Medico actualizarMedico(Long id, Medico medicoDetalles) {
        Medico medico = obtenerPorId(id);
        medico.setNombre(medicoDetalles.getNombre());
        medico.setApellido(medicoDetalles.getApellido());
        medico.setCmp(medicoDetalles.getCmp());
        medico.setTelefono(medicoDetalles.getTelefono());
        medico.setEmail(medicoDetalles.getEmail());
        return medicoRepository.save(medico);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Medico> obtenerTodos() {
        return medicoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Medico obtenerPorId(Long id) {
        return medicoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Médico no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public Medico cambiarEstado(Long id, Boolean estado) {
        Medico medico = obtenerPorId(id);
        medico.setEstado(estado);
        return medicoRepository.save(medico);
    }

    @Override
    @Transactional
    public Medico asociarEspecialidades(Long medicoId, List<Long> especialidadIds) {
        Medico medico = obtenerPorId(medicoId);
        List<Especialidad> especialidades = especialidadRepository.findAllById(especialidadIds);

        if (especialidades.size() != especialidadIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Una o más especialidades no existen");
        }

        for (Especialidad esp : especialidades) {
            if (!medico.getEspecialidades().contains(esp)) {
                medico.getEspecialidades().add(esp);
            }
        }
        return medicoRepository.save(medico);
    }
}