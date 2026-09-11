package com.hospital.medicos.service.impl;

import com.hospital.medicos.entity.Consultorio;
import com.hospital.medicos.entity.Horario;
import com.hospital.medicos.repository.ConsultorioRepository;
import com.hospital.medicos.repository.HorarioRepository;
import com.hospital.medicos.service.ConsultorioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ConsultorioServiceImpl implements ConsultorioService {

    private final ConsultorioRepository consultorioRepository;
    private final HorarioRepository horarioRepository;

    @Autowired
    public ConsultorioServiceImpl(ConsultorioRepository consultorioRepository, HorarioRepository horarioRepository) {
        this.consultorioRepository = consultorioRepository;
        this.horarioRepository = horarioRepository;
    }

    @Override
    @Transactional
    public Consultorio registrarConsultorio(Consultorio consultorio) {
        if (consultorioRepository.existsByCodigo(consultorio.getCodigo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un consultorio con el código: " + consultorio.getCodigo());
        }
        consultorio.setEstado("DISPONIBLE");
        return consultorioRepository.save(consultorio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Consultorio> obtenerTodos() {
        return consultorioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Consultorio obtenerPorId(Long id) {
        return consultorioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Consultorio no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public Horario asignarConsultorioAHorario(Long horarioId, Long consultorioId) {
        Horario horario = horarioRepository.findById(horarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Horario no encontrado con ID: " + horarioId));
        Consultorio consultorio = obtenerPorId(consultorioId);

        // Evita que dos médicos compartan el mismo consultorio en el mismo día/hora
        List<Horario> horariosEnConsultorio = horarioRepository.findByConsultorioIdAndDiaSemana(consultorioId, horario.getDiaSemana());
        for (Horario h : horariosEnConsultorio) {
            if (!h.getId().equals(horarioId)
                    && !h.getMedico().getId().equals(horario.getMedico().getId())
                    && h.getHoraInicio().isBefore(horario.getHoraFin())
                    && horario.getHoraInicio().isBefore(h.getHoraFin())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "El consultorio " + consultorio.getCodigo() + " ya está asignado a otro médico en ese horario");
            }
        }

        horario.setConsultorio(consultorio);
        return horarioRepository.save(horario);
    }
}