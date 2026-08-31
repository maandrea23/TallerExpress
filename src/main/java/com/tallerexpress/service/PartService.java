package com.tallerexpress.service;

import com.tallerexpress.config.Database;
import com.tallerexpress.dao.*;
import com.tallerexpress.exception.*;
import com.tallerexpress.model.Part;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class PartService {
  private final PartDao dao = new JdbcPartDao();

  public Part save(Part p) {
    validate(p);
    try (Connection c = Database.getConnection()) {
      if (dao.existsByCode(c, p.code(), p.id()))
        throw new BusinessException("El código de referencia ya existe");
      log(p.id() == null ? "POST" : "PATCH", "/repuestos");
      if (p.id() == null)
        return dao.create(
            c,
            new Part(
                null,
                p.code().trim().toUpperCase(),
                p.name(),
                p.category(),
                p.supplier(),
                p.stockTotal(),
                p.stockAvailable(),
                p.unitPrice(),
                p.active(),
                LocalDateTime.now()));
      dao.update(c, p);
      return p;
    } catch (SQLException e) {
      throw new DataAccessException("Error de conexión", e);
    }
  }

  public List<Part> list(String cat, String sup) {
    try (Connection c = Database.getConnection()) {
      log("GET", "/repuestos");
      return dao.filter(c, cat == null ? "" : cat.trim(), sup == null ? "" : sup.trim());
    } catch (SQLException e) {
      throw new DataAccessException("Error de conexión", e);
    }
  }

  private void validate(Part p) {
    if (p.code() == null || p.code().isBlank() || p.name() == null || p.name().isBlank())
      throw new BusinessException("Código y nombre son obligatorios");
    if (p.stockTotal() < 0 || p.stockAvailable() < 0 || p.stockAvailable() > p.stockTotal())
      throw new BusinessException("El stock disponible debe estar entre cero y el stock total");
    if (p.unitPrice() == null || p.unitPrice().signum() < 0)
      throw new BusinessException("El precio no puede ser negativo");
  }

  private void log(String m, String p) {
    System.out.printf("[HTTP] %s %s%n", m, p);
  }
}
