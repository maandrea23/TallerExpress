package com.tallerexpress.dao.impl;
import com.tallerexpress.dao.VehicleDao;
import com.tallerexpress.exception.DataAccessException;
import com.tallerexpress.model.Vehicle;
import java.sql.*;
import java.util.*;

public final class JdbcVehicleDao implements VehicleDao {
  public Vehicle create(Connection c, Vehicle v) {
    try (PreparedStatement q = c.prepareStatement(
        "INSERT INTO vehicles(client_id,plate,brand,model,vehicle_year) VALUES(?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
      q.setLong(1, v.clientId()); q.setString(2, v.plate()); q.setString(3, v.brand()); q.setString(4, v.model()); q.setInt(5, v.year());
      q.executeUpdate();
      try (ResultSet r = q.getGeneratedKeys()) { r.next(); return new Vehicle(r.getLong(1), v.clientId(),
          v.plate(), v.brand(), v.model(), v.year(), v.createdAt()); }
    } catch (SQLException e) { throw error("Error creando el vehículo", e); }
  }
  public List<Vehicle> findAll(Connection c, Long clientId) {
    List<Vehicle> out = new ArrayList<>();
    String sql = "SELECT * FROM vehicles" + (clientId == null ? "" : " WHERE client_id=?") + " ORDER BY id";
    try (PreparedStatement q = c.prepareStatement(sql)) {
      if (clientId != null) q.setLong(1, clientId);
      try (ResultSet r = q.executeQuery()) { while (r.next()) out.add(new Vehicle(r.getLong("id"),
          r.getLong("client_id"), r.getString("plate"), r.getString("brand"), r.getString("model"),
          r.getInt("vehicle_year"), r.getTimestamp("created_at").toLocalDateTime())); }
      return out;
    } catch (SQLException e) { throw error("Error consultando vehículos", e); }
  }
  public boolean belongsToActiveClient(Connection c, long vehicleId, long clientId) {
    try (PreparedStatement q = c.prepareStatement(
        "SELECT v.id FROM vehicles v JOIN clients cl ON cl.id=v.client_id WHERE v.id=? AND v.client_id=? AND cl.active=TRUE")) {
      q.setLong(1, vehicleId); q.setLong(2, clientId); try (ResultSet r = q.executeQuery()) { return r.next(); }
    } catch (SQLException e) { throw error("Error validando el vehículo", e); }
  }
  private DataAccessException error(String message, SQLException e) { return new DataAccessException(message, e); }
}
