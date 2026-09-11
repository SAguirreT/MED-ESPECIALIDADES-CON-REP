package com.hospital.medicos.service.impl;

import com.hospital.medicos.entity.BloqueoHorario;
import com.hospital.medicos.entity.Horario;
import com.hospital.medicos.entity.Medico;
import com.hospital.medicos.repository.BloqueoHorarioRepository;
import com.hospital.medicos.repository.HorarioRepository;
import com.hospital.medicos.repository.MedicoRepository;
import com.hospital.medicos.service.HorarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class HorarioServiceImpl implements HorarioService {

    private final HorarioRepository horarioRepository;
    private final BloqueoHorarioRepository bloqueoHorarioRepository;
    private final MedicoRepository medicoRepository;

    @Autowired
    public HorarioServiceImpl(HorarioRepository horarioRepository,
                              BloqueoHorarioRepository bloqueoHorarioRepository,
                              MedicoRepository medicoRepository) {
        this.horarioRepository = horarioRepository;
        this.bloqueoHorarioRepository = bloqueoHorarioRepository;
        this.medicoRepository = medicoRepository;
    }

    private Medico obtenerMedico(Long medicoId) {
        return medicoRepository.findById(medicoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Médico no encontrado con ID: " + medicoId));
    }

    private void validarRangoHoras(java.time.LocalTime inicio, java.time.LocalTime fin) {
        if (inicio != null && fin != null && !fin.isAfter(inicio)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La hora de fin debe ser posterior a la hora de inicio");
        }
    }

    @Override
    @Transactional
    public Horario registrarHorario(Long medicoId, Horario horario) {
        Medico medico = obtenerMedico(medicoId);
        validarRangoHoras(horario.getHoraInicio(), horario.getHoraFin());
        horario.setMedico(medico);
        horario.setEstado(true);
        return horarioRepository.save(horario);
    }

    @Override
    @Transactional
    public Horario actualizarHorario(Long id, Horario detalles) {
        Horario horario = obtenerPorId(id);
        validarRangoHoras(detalles.getHoraInicio(), detalles.getHoraFin());
        horario.setDiaSemana(detalles.getDiaSemana());
        horario.setHoraInicio(detalles.getHoraInicio());
        horario.setHoraFin(detalles.getHoraFin());
        horario.setDuracionCitaMinutos(detalles.getDuracionCitaMinutos());
        return horarioRepository.save(horario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Horario> obtenerPorMedico(Long medicoId) {
        return horarioRepository.findByMedicoId(medicoId);
    }

    @Override
    @Transactional(readOnly = true)
    public Horario obtenerPorId(Long id) {
        return horarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Horario no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public BloqueoHorario bloquearHorario(Long medicoId, BloqueoHorario bloqueo) {
        Medico medico = obtenerMedico(medicoId);
        validarRangoHoras(bloqueo.getHoraInicio(), bloqueo.getHoraFin());
        bloqueo.setMedico(medico);
        bloqueo.setEstado("BLOQUEADO");
        return bloqueoHorarioRepository.save(bloqueo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BloqueoHorario> obtenerBloqueosPorMedico(Long medicoId) {
        return bloqueoHorarioRepository.findByMedicoId(medicoId);
    }
}