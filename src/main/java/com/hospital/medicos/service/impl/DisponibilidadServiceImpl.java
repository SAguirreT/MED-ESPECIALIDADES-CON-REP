package com.hospital.medicos.service.impl;

import com.hospital.medicos.dto.SlotDisponibilidadDTO;
import com.hospital.medicos.entity.BloqueoHorario;
import com.hospital.medicos.entity.DiaSemana;
import com.hospital.medicos.entity.Horario;
import com.hospital.medicos.entity.VacacionPermiso;
import com.hospital.medicos.repository.BloqueoHorarioRepository;
import com.hospital.medicos.repository.HorarioRepository;
import com.hospital.medicos.repository.VacacionPermisoRepository;
import com.hospital.medicos.service.DisponibilidadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DisponibilidadServiceImpl implements DisponibilidadService {

    private final HorarioRepository horarioRepository;
    private final BloqueoHorarioRepository bloqueoHorarioRepository;
    private final VacacionPermisoRepository vacacionPermisoRepository;

    private static final DiaSemana[] DIAS = {
            DiaSemana.LUNES, DiaSemana.MARTES, DiaSemana.MIERCOLES,
            DiaSemana.JUEVES, DiaSemana.VIERNES, DiaSemana.SABADO, DiaSemana.DOMINGO
    };

    @Autowired
    public DisponibilidadServiceImpl(HorarioRepository horarioRepository,
                                     BloqueoHorarioRepository bloqueoHorarioRepository,
                                     VacacionPermisoRepository vacacionPermisoRepository) {
        this.horarioRepository = horarioRepository;
        this.bloqueoHorarioRepository = bloqueoHorarioRepository;
        this.vacacionPermisoRepository = vacacionPermisoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotDisponibilidadDTO> calcularDisponibilidad(Long medicoId, LocalDate fecha) {
        List<SlotDisponibilidadDTO> resultado = new ArrayList<>();

        // 1. ¿El médico está de vacaciones/permiso ese día?
        List<VacacionPermiso> vacaciones = vacacionPermisoRepository.findByMedicoId(medicoId);
        for (VacacionPermiso v : vacaciones) {
            if (!fecha.isBefore(v.getFechaInicio()) && !fecha.isAfter(v.getFechaFin())) {
                resultado.add(new SlotDisponibilidadDTO(null, null, false,
                        "Médico no disponible: " + v.getMotivo()));
                return resultado; // todo el día bloqueado, no hace falta seguir
            }
        }

        // 2. Horarios recurrentes del médico para ese día de la semana
        DiaSemana diaSemana = DIAS[fecha.getDayOfWeek().getValue() - 1];
        List<Horario> horarios = horarioRepository.findByMedicoId(medicoId);
        List<BloqueoHorario> bloqueos = bloqueoHorarioRepository.findByMedicoId(medicoId);

        for (Horario h : horarios) {
            if (!h.getDiaSemana().equals(diaSemana) || !Boolean.TRUE.equals(h.getEstado())) continue;

            LocalTime cursor = h.getHoraInicio();
            int duracion = h.getDuracionCitaMinutos();

            while (cursor.plusMinutes(duracion).compareTo(h.getHoraFin()) <= 0) {
                LocalTime finSlot = cursor.plusMinutes(duracion);
                boolean disponible = true;
                String motivo = null;

                for (BloqueoHorario b : bloqueos) {
                    if (b.getFecha().equals(fecha)
                            && cursor.isBefore(b.getHoraFin())
                            && b.getHoraInicio().isBefore(finSlot)) {
                        disponible = false;
                        motivo = "Bloqueado: " + b.getMotivo();
                        break;
                    }
                }

                // NOTA: aquí falta descontar citas ya reservadas (módulo de Citas,
                // a cargo de otro integrante). Cuando esté listo, agregar una
                // verificación similar consultando el repositorio de Citas.

                resultado.add(new SlotDisponibilidadDTO(cursor, finSlot, disponible, motivo));
                cursor = finSlot;
            }
        }

        return resultado;
    }
}