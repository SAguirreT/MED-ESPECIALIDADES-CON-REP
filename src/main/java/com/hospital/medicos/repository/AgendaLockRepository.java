package com.hospital.medicos.repository;

import com.hospital.medicos.entity.AgendaLock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;

public interface AgendaLockRepository extends JpaRepository<AgendaLock, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select l from AgendaLock l where l.id=1")
  AgendaLock acquire();
}
