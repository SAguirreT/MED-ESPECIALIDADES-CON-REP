package com.hospital.medicos.repository;

import com.hospital.medicos.entity.VacacionPermiso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VacacionPermisoRepository extends JpaRepository<VacacionPermiso, Long> {
    List<VacacionPermiso> findByMedicoId(Long medicoId);
}