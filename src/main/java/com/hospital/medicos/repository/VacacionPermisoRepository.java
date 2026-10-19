package com.hospital.medicos.repository;

import com.hospital.medicos.entity.VacacionPermiso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VacacionPermisoRepository extends JpaRepository<VacacionPermiso, Long> {
  java.util.List<VacacionPermiso> findByMedicoId(Long id);
}
