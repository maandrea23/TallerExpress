package com.tallerexpress.dao.impl;

import com.tallerexpress.dao.PartDao;
import com.tallerexpress.exception.DataAccessException;
import com.tallerexpress.model.Part;
import java.sql.*;
import java.util.*;

public class JdbcPartDao implements PartDao {
  public Part create(Connection c, Part p) {
    String s =
        "INSERT INTO parts(code,name,category,supplier,stock_total,stock_available,unit_price,active) VALUES(?,?,?,?,?,?,?,?)";
    try (PreparedStatement q = c.prepareStatement(s, Statement.RETURN_GENERATED_KEYS)) {
      bind(q, p);
      q.executeUpdate();
      try (ResultSet r = q.getGeneratedKeys()) {
        r.next();
        return new Part(
            r.getLong(1),
            p.code(),
            p.name(),
            p.category(),
            p.supplier(),
            p.stockTotal(),
            p.stockAvailable(),
            p.unitPrice(),
            p.active(),
            p.createdAt());
      }
    } catch (SQLException e) {
      throw new DataAccessException("No se pudo guardar el repuesto", e);
    }
  }

  public void update(Connection c, Part p) {
    try (PreparedStatement q =
        c.prepareStatement(
            "UPDATE parts SET code=?,name=?,category=?,supplier=?,stock_total=?,stock_available=?,unit_price=?,active=? WHERE id=?")) {
      bind(q, p);
      q.setLong(9, p.id());
      q.executeUpdate();
    } catch (SQLException e) {
      throw new DataAccessException("No se pudo actualizar el repuesto", e);
    }
  }

  private void bind(PreparedStatement q, Part p) throws SQLException {
    q.setString(1, p.code());
    q.setString(2, p.name());
    q.setString(3, p.category());
    q.setString(4, p.supplier());
    q.setInt(5, p.stockTotal());
    q.setInt(6, p.stockAvailable());
    q.setBigDecimal(7, p.unitPrice());
    q.setBoolean(8, p.active());
  }

  public List<Part> findAll(Connection c) {
    return filter(c, "", "");
  }

  public boolean existsByCode(Connection c, String code, Long excluded) {
    String s =
        "SELECT COUNT(*) FROM parts WHERE UPPER(code)=UPPER(?)"
            + (excluded == null ? "" : " AND id<>?");
    try (PreparedStatement q = c.prepareStatement(s)) {
      q.setString(1, code);
      if (excluded != null) q.setLong(2, excluded);
      try (ResultSet r = q.executeQuery()) {
        r.next();
        return r.getInt(1) > 0;
      }
    } catch (SQLException e) {
      throw new DataAccessException("Error validando código", e);
    }
  }

  public List<Part> filter(Connection c, String cat, String sup) {
    List<Part> out = new ArrayList<>();
    try (PreparedStatement q =
        c.prepareStatement(
            "SELECT * FROM parts WHERE UPPER(category) LIKE UPPER(?) AND UPPER(supplier) LIKE UPPER(?) ORDER BY id")) {
      q.setString(1, "%" + cat + "%");
      q.setString(2, "%" + sup + "%");
      try (ResultSet r = q.executeQuery()) {
        while (r.next())
          out.add(
              new Part(
                  r.getLong("id"),
                  r.getString("code"),
                  r.getString("name"),
                  r.getString("category"),
                  r.getString("supplier"),
                  r.getInt("stock_total"),
                  r.getInt("stock_available"),
                  r.getBigDecimal("unit_price"),
                  r.getBoolean("active"),
                  r.getTimestamp("created_at").toLocalDateTime()));
      }
      return out;
    } catch (SQLException e) {
      throw new DataAccessException("Error consultando repuestos", e);
    }
  }
}
