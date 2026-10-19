package com.hospital.medicos.repository;

import com.hospital.medicos.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
  org.springframework.data.domain.Page<Auditoria>
      findByUsuarioContainingIgnoreCaseAndTablaContainingIgnoreCase(
          String usuario, String tabla, org.springframework.data.domain.Pageable pageable);
}
