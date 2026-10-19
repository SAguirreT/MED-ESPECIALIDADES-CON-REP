package com.hospital.medicos.repository;

import com.hospital.medicos.entity.Medico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicoRepository extends JpaRepository<Medico, Long> {
  boolean existsByCmpAndIdNot(String cmp, Long id);

  boolean existsByCmp(String cmp);
}
