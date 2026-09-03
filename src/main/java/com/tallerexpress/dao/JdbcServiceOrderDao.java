package com.tallerexpress.dao;
import com.tallerexpress.exception.DataAccessException;
import com.tallerexpress.model.ServiceOrder;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public final class JdbcServiceOrderDao implements ServiceOrderDao {
  public long create(Connection c, ServiceOrder o) {
    try (PreparedStatement q = c.prepareStatement(
        "INSERT INTO service_orders(client_id,vehicle_id,mechanic,problem,diagnosis,status) VALUES(?,?,?,?,?,'ABIERTA')",
        Statement.RETURN_GENERATED_KEYS)) {
      q.setLong(1, o.clientId()); q.setLong(2, o.vehicleId()); q.setString(3, o.mechanic());
      q.setString(4, o.problem()); q.setString(5, o.diagnosis()); q.executeUpdate();
      try (ResultSet r = q.getGeneratedKeys()) { r.next(); return r.getLong(1); }
    } catch (SQLException e) { throw error("Error creando la orden", e); }
  }
  public Optional<PartAvailability> lockPart(Connection c, long partId) {
    try (PreparedStatement q = c.prepareStatement(
        "SELECT unit_price,stock_available,active FROM parts WHERE id=? FOR UPDATE")) {
      q.setLong(1, partId); try (ResultSet r = q.executeQuery()) {
        return r.next() ? Optional.of(new PartAvailability(r.getBigDecimal("unit_price"),
            r.getInt("stock_available"), r.getBoolean("active"))) : Optional.empty(); }
    } catch (SQLException e) { throw error("Error consultando el repuesto", e); }
  }
  public void addPart(Connection c, long orderId, long partId, int quantity, BigDecimal price) {
    try (PreparedStatement q = c.prepareStatement("INSERT INTO order_parts VALUES(?,?,?,?)")) {
      q.setLong(1, orderId); q.setLong(2, partId); q.setInt(3, quantity); q.setBigDecimal(4, price); q.executeUpdate();
    } catch (SQLException e) { throw error("Error agregando el repuesto a la orden", e); }
  }
  public void decreaseStock(Connection c, long partId, int quantity) {
    try (PreparedStatement q = c.prepareStatement("UPDATE parts SET stock_available=stock_available-? WHERE id=?")) {
      q.setInt(1, quantity); q.setLong(2, partId); q.executeUpdate();
    } catch (SQLException e) { throw error("Error actualizando el inventario", e); }
  }
  public Optional<String> lockStatus(Connection c, long orderId) {
    try (PreparedStatement q = c.prepareStatement("SELECT status FROM service_orders WHERE id=? FOR UPDATE")) {
      q.setLong(1, orderId); try (ResultSet r = q.executeQuery()) {
        return r.next() ? Optional.ofNullable(r.getString(1)) : Optional.empty(); }
    } catch (SQLException e) { throw error("Error consultando la orden", e); }
  }
  public BigDecimal calculateTotal(Connection c, long orderId) {
    try (PreparedStatement q = c.prepareStatement(
        "SELECT COALESCE(SUM(quantity*unit_price),0) FROM order_parts WHERE order_id=?")) {
      q.setLong(1, orderId); try (ResultSet r = q.executeQuery()) { r.next(); return r.getBigDecimal(1); }
    } catch (SQLException e) { throw error("Error calculando el total", e); }
  }
  public void finish(Connection c, long orderId, BigDecimal total) {
    try (PreparedStatement q = c.prepareStatement(
        "UPDATE service_orders SET status='FINALIZADA',final_cost=? WHERE id=?")) {
      q.setBigDecimal(1, total); q.setLong(2, orderId); q.executeUpdate();
    } catch (SQLException e) { throw error("Error finalizando la orden", e); }
  }
  public List<ServiceOrder> findAll(Connection c, Long vehicleId) {
    List<ServiceOrder> out = new ArrayList<>();
    String sql = "SELECT * FROM service_orders" + (vehicleId == null ? "" : " WHERE vehicle_id=?") + " ORDER BY id";
    try (PreparedStatement q = c.prepareStatement(sql)) {
      if (vehicleId != null) q.setLong(1, vehicleId);
      try (ResultSet r = q.executeQuery()) { while (r.next()) out.add(new ServiceOrder(r.getLong("id"),
          r.getLong("client_id"), r.getLong("vehicle_id"), r.getString("mechanic"),
          r.getTimestamp("entry_date").toLocalDateTime(), r.getString("problem"), r.getString("diagnosis"),
          r.getString("status"), r.getBigDecimal("final_cost"))); }
      return out;
    } catch (SQLException e) { throw error("Error consultando órdenes", e); }
  }
  private DataAccessException error(String message, SQLException e) { return new DataAccessException(message, e); }
}
