package com.hospital.medicos.controller;

import java.util.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<?> business(ResponseStatusException e) {
    return ResponseEntity.status(e.getStatusCode())
        .body(
            Map.of(
                "status",
                e.getStatusCode().value(),
                "message",
                Objects.requireNonNullElse(e.getReason(), "Operación rechazada")));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<?> validation(MethodArgumentNotValidException e) {
    var fields = new LinkedHashMap<String, String>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(f -> fields.putIfAbsent(f.getField(), f.getDefaultMessage()));
    return ResponseEntity.badRequest()
        .body(
            Map.of("status", 400, "message", "Revisa los campos del formulario", "fields", fields));
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class
  })
  ResponseEntity<?> malformed(Exception e) {
    return ResponseEntity.badRequest()
        .body(Map.of("status", 400, "message", "Formato de solicitud inválido"));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<?> duplicate(DataIntegrityViolationException e) {
    return ResponseEntity.status(409)
        .body(
            Map.of(
                "status",
                409,
                "message",
                "El registro está duplicado o tiene relaciones incompatibles"));
  }

  @ExceptionHandler(AuthenticationException.class)
  ResponseEntity<?> auth(AuthenticationException e) {
    return ResponseEntity.status(401)
        .body(Map.of("status", 401, "message", "Credenciales incorrectas o usuario inactivo"));
  }
}
