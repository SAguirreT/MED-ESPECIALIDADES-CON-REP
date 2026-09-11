package com.hospital.medicos.repository;

import com.hospital.medicos.entity.Consultorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultorioRepository extends JpaRepository<Consultorio, Long> {
    boolean existsByCodigo(String codigo);
}