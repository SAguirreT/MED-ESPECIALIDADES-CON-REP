package com.hospital.medicos.service;

import com.hospital.medicos.entity.Auditoria;
import com.hospital.medicos.repository.AuditoriaRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {
  private final AuditoriaRepository repository;

  public AuditService(AuditoriaRepository repository) {
    this.repository = repository;
  }

  @Transactional
  public void record(String action, String table, Long id, String detail) {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    recordAs(auth == null ? "SISTEMA" : auth.getName(), action, table, id, detail);
  }

  @Transactional
  public void recordAs(String user, String action, String table, Long id, String detail) {
    var row = new Auditoria();
    row.setUsuario(user);
    row.setAccion(action);
    row.setTabla(table);
    row.setRegistroId(id);
    row.setDetalle(detail);
    row.setFechaHora(OffsetDateTime.now(ZoneOffset.UTC));
    repository.save(row);
  }
}
