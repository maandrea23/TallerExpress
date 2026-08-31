package com.tallerexpress.service;

import com.tallerexpress.config.Database;
import com.tallerexpress.exception.*;
import com.tallerexpress.model.*;
import com.tallerexpress.service.user.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/** Servicios JDBC para clientes, vehículos, usuarios y órdenes. */
public class WorkshopService {
  private final UserCreator userCreator = new DefaultPropertiesUserCreator(new BaseUserCreator());

  public User login(String username, String password) {
    log("POST", "/login");
    String s = "SELECT * FROM users WHERE username=? AND password=? AND status='ACTIVO'";
    try (Connection c = Database.getConnection();
        PreparedStatement q = c.prepareStatement(s)) {
      q.setString(1, username);
      q.setString(2, password);
      try (ResultSet r = q.executeQuery()) {
        if (!r.next()) throw new BusinessException("Credenciales incorrectas o usuario inactivo");
        return user(r);
      }
    } catch (SQLException e) {
      throw data(e);
    }
  }

  public User createUser(String username, String password, String name) {
    if (blank(username) || blank(password) || blank(name))
      throw new BusinessException("Todos los datos del usuario son obligatorios");
    User u =
        userCreator.create(
            new User(null, username.trim(), password, name.trim(), null, null, null));
    log("POST", "/usuarios");
    try (Connection c = Database.getConnection();
        PreparedStatement q =
            c.prepareStatement(
                "INSERT INTO users(username,password,full_name,role,status,created_at) VALUES(?,?,?,?,?,?)",
                Statement.RETURN_GENERATED_KEYS)) {
      q.setString(1, u.username());
      q.setString(2, u.password());
      q.setString(3, u.fullName());
      q.setString(4, u.role());
      q.setString(5, u.status());
      q.setTimestamp(6, Timestamp.valueOf(u.createdAt()));
      q.executeUpdate();
      try (ResultSet r = q.getGeneratedKeys()) {
        r.next();
        return new User(
            r.getLong(1),
            u.username(),
            u.password(),
            u.fullName(),
            u.role(),
            u.status(),
            u.createdAt());
      }
    } catch (SQLException e) {
      if (unique(e)) throw new BusinessException("El nombre de usuario ya existe");
      throw data(e);
    }
  }

  public List<User> users() {
    List<User> out = new ArrayList<>();
    try (Connection c = Database.getConnection();
        Statement q = c.createStatement();
        ResultSet r = q.executeQuery("SELECT * FROM users ORDER BY id")) {
      while (r.next()) out.add(user(r));
      log("GET", "/usuarios");
      return out;
    } catch (SQLException e) {
      throw data(e);
    }
  }

  public void toggleUser(long id) {
    try (Connection c = Database.getConnection();
        PreparedStatement q =
            c.prepareStatement(
                "UPDATE users SET status=CASE status WHEN 'ACTIVO' THEN 'INACTIVO' ELSE 'ACTIVO' END WHERE id=?")) {
      q.setLong(1, id);
      if (q.executeUpdate() == 0) throw new BusinessException("Usuario inexistente");
      log("PATCH", "/usuarios/" + id);
    } catch (SQLException e) {
      throw data(e);
    }
  }

  public void deleteUser(long id) {
    try (Connection c = Database.getConnection();
        PreparedStatement q = c.prepareStatement("DELETE FROM users WHERE id=?")) {
      q.setLong(1, id);
      if (q.executeUpdate() == 0) throw new BusinessException("Usuario inexistente");
      log("DELETE", "/usuarios/" + id);
    } catch (SQLException e) {
      throw data(e);
    }
  }

  public Client createClient(String doc, String name, String phone, String email) {
    if (blank(doc) || blank(name))
      throw new BusinessException("Documento y nombre son obligatorios");
    try (Connection c = Database.getConnection();
        PreparedStatement q =
            c.prepareStatement(
                "INSERT INTO clients(document,name,phone,email,active) VALUES(?,?,?,?,TRUE)",
                Statement.RETURN_GENERATED_KEYS)) {
      q.setString(1, doc.trim());
      q.setString(2, name.trim());
      q.setString(3, phone);
      q.setString(4, email);
      q.executeUpdate();
      try (ResultSet r = q.getGeneratedKeys()) {
        r.next();
        log("POST", "/clientes");
        return new Client(r.getLong(1), doc, name, phone, email, true, LocalDateTime.now());
      }
    } catch (SQLException e) {
      if (unique(e)) throw new BusinessException("El documento ya está registrado");
      throw data(e);
    }
  }

  public List<Client> clients() {
    List<Client> out = new ArrayList<>();
    try (Connection c = Database.getConnection();
        Statement q = c.createStatement();
        ResultSet r = q.executeQuery("SELECT * FROM clients ORDER BY id")) {
      while (r.next())
        out.add(
            new Client(
                r.getLong("id"),
                r.getString("document"),
                r.getString("name"),
                r.getString("phone"),
                r.getString("email"),
                r.getBoolean("active"),
                r.getTimestamp("created_at").toLocalDateTime()));
      log("GET", "/clientes");
      return out;
    } catch (SQLException e) {
      throw data(e);
    }
  }

  public Vehicle createVehicle(long clientId, String plate, String brand, String model, int year) {
    if (blank(plate) || blank(brand) || blank(model))
      throw new BusinessException("Placa, marca y modelo son obligatorios");
    try (Connection c = Database.getConnection()) {
      try (PreparedStatement check = c.prepareStatement("SELECT active FROM clients WHERE id=?")) {
        check.setLong(1, clientId);
        try (ResultSet r = check.executeQuery()) {
          if (!r.next() || !r.getBoolean(1))
            throw new BusinessException("El cliente no existe o está inactivo");
        }
      }
      try (PreparedStatement q =
          c.prepareStatement(
              "INSERT INTO vehicles(client_id,plate,brand,model,vehicle_year) VALUES(?,?,?,?,?)",
              Statement.RETURN_GENERATED_KEYS)) {
        q.setLong(1, clientId);
        q.setString(2, plate.trim().toUpperCase());
        q.setString(3, brand);
        q.setString(4, model);
        q.setInt(5, year);
        q.executeUpdate();
        try (ResultSet r = q.getGeneratedKeys()) {
          r.next();
          log("POST", "/vehiculos");
          return new Vehicle(
              r.getLong(1), clientId, plate, brand, model, year, LocalDateTime.now());
        }
      }
    } catch (SQLException e) {
      if (unique(e)) throw new BusinessException("La placa ya está registrada");
      throw data(e);
    }
  }

  public List<Vehicle> vehicles(Long clientId) {
    List<Vehicle> out = new ArrayList<>();
    String s =
        "SELECT * FROM vehicles" + (clientId == null ? "" : " WHERE client_id=?") + " ORDER BY id";
    try (Connection c = Database.getConnection();
        PreparedStatement q = c.prepareStatement(s)) {
      if (clientId != null) q.setLong(1, clientId);
      try (ResultSet r = q.executeQuery()) {
        while (r.next())
          out.add(
              new Vehicle(
                  r.getLong("id"),
                  r.getLong("client_id"),
                  r.getString("plate"),
                  r.getString("brand"),
                  r.getString("model"),
                  r.getInt("vehicle_year"),
                  r.getTimestamp("created_at").toLocalDateTime()));
      }
      log("GET", "/vehiculos");
      return out;
    } catch (SQLException e) {
      throw data(e);
    }
  }

  public long createOrder(
      long clientId,
      long vehicleId,
      String mechanic,
      String problem,
      String diagnosis,
      List<OrderPart> parts) {
    if (blank(mechanic) || blank(problem))
      throw new BusinessException("Mecánico y descripción del problema son obligatorios");
    Connection c = null;
    try {
      c = Database.getConnection();
      c.setAutoCommit(false);
      try (PreparedStatement v =
          c.prepareStatement(
              "SELECT v.id FROM vehicles v JOIN clients cl ON cl.id=v.client_id WHERE v.id=? AND v.client_id=? AND cl.active=TRUE")) {
        v.setLong(1, vehicleId);
        v.setLong(2, clientId);
        try (ResultSet r = v.executeQuery()) {
          if (!r.next())
            throw new BusinessException("El vehículo no pertenece a un cliente activo");
        }
      }
      long id;
      try (PreparedStatement q =
          c.prepareStatement(
              "INSERT INTO service_orders(client_id,vehicle_id,mechanic,problem,diagnosis,status) VALUES(?,?,?,?,?,'ABIERTA')",
              Statement.RETURN_GENERATED_KEYS)) {
        q.setLong(1, clientId);
        q.setLong(2, vehicleId);
        q.setString(3, mechanic);
        q.setString(4, problem);
        q.setString(5, diagnosis);
        q.executeUpdate();
        try (ResultSet r = q.getGeneratedKeys()) {
          r.next();
          id = r.getLong(1);
        }
      }
      for (OrderPart p : parts) addPart(c, id, p);
      c.commit();
      log("POST", "/ordenes");
      return id;
    } catch (Exception e) {
      rollback(c);
      if (e instanceof BusinessException b) throw b;
      if (e instanceof SQLException s) throw data(s);
      throw new BusinessException("No se pudo registrar la orden: " + e.getMessage());
    } finally {
      close(c);
    }
  }

  private void addPart(Connection c, long orderId, OrderPart p) throws SQLException {
    if (p.quantity() <= 0) throw new BusinessException("La cantidad debe ser mayor que cero");
    BigDecimal price;
    try (PreparedStatement q =
        c.prepareStatement(
            "SELECT unit_price,stock_available,active FROM parts WHERE id=? FOR UPDATE")) {
      q.setLong(1, p.partId());
      try (ResultSet r = q.executeQuery()) {
        if (!r.next() || !r.getBoolean("active"))
          throw new BusinessException("Repuesto inexistente o inactivo: " + p.partId());
        if (r.getInt("stock_available") < p.quantity())
          throw new BusinessException("Stock insuficiente para el repuesto " + p.partId());
        price = r.getBigDecimal("unit_price");
      }
    }
    try (PreparedStatement q = c.prepareStatement("INSERT INTO order_parts VALUES(?,?,?,?)")) {
      q.setLong(1, orderId);
      q.setLong(2, p.partId());
      q.setInt(3, p.quantity());
      q.setBigDecimal(4, price);
      q.executeUpdate();
    }
    try (PreparedStatement q =
        c.prepareStatement("UPDATE parts SET stock_available=stock_available-? WHERE id=?")) {
      q.setInt(1, p.quantity());
      q.setLong(2, p.partId());
      q.executeUpdate();
    }
  }

  public BigDecimal finishOrder(long id) {
    Connection c = null;
    try {
      c = Database.getConnection();
      c.setAutoCommit(false);
      BigDecimal total;
      try (PreparedStatement q =
          c.prepareStatement("SELECT status FROM service_orders WHERE id=? FOR UPDATE")) {
        q.setLong(1, id);
        try (ResultSet r = q.executeQuery()) {
          if (!r.next() || !"ABIERTA".equals(r.getString(1)))
            throw new BusinessException("La orden no existe o ya fue finalizada");
        }
      }
      try (PreparedStatement q =
          c.prepareStatement(
              "SELECT COALESCE(SUM(quantity*unit_price),0) FROM order_parts WHERE order_id=?")) {
        q.setLong(1, id);
        try (ResultSet r = q.executeQuery()) {
          r.next();
          total = r.getBigDecimal(1);
        }
      }
      try (PreparedStatement q =
          c.prepareStatement(
              "UPDATE service_orders SET status='FINALIZADA',final_cost=? WHERE id=?")) {
        q.setBigDecimal(1, total);
        q.setLong(2, id);
        q.executeUpdate();
      }
      c.commit();
      log("PATCH", "/ordenes/" + id);
      return total;
    } catch (Exception e) {
      rollback(c);
      if (e instanceof BusinessException b) throw b;
      if (e instanceof SQLException s) throw data(s);
      throw new BusinessException("No se pudo finalizar la orden: " + e.getMessage());
    } finally {
      close(c);
    }
  }

  public List<ServiceOrder> orders(Long vehicleId) {
    List<ServiceOrder> out = new ArrayList<>();
    String s =
        "SELECT * FROM service_orders"
            + (vehicleId == null ? "" : " WHERE vehicle_id=?")
            + " ORDER BY id";
    try (Connection c = Database.getConnection();
        PreparedStatement q = c.prepareStatement(s)) {
      if (vehicleId != null) q.setLong(1, vehicleId);
      try (ResultSet r = q.executeQuery()) {
        while (r.next()) {
          Timestamp t = r.getTimestamp("entry_date");
          out.add(
              new ServiceOrder(
                  r.getLong("id"),
                  r.getLong("client_id"),
                  r.getLong("vehicle_id"),
                  r.getString("mechanic"),
                  t.toLocalDateTime(),
                  r.getString("problem"),
                  r.getString("diagnosis"),
                  r.getString("status"),
                  r.getBigDecimal("final_cost")));
        }
      }
      return out;
    } catch (SQLException e) {
      throw data(e);
    }
  }

  private User user(ResultSet r) throws SQLException {
    return new User(
        r.getLong("id"),
        r.getString("username"),
        r.getString("password"),
        r.getString("full_name"),
        r.getString("role"),
        r.getString("status"),
        r.getTimestamp("created_at").toLocalDateTime());
  }

  private boolean blank(String s) {
    return s == null || s.isBlank();
  }

  private boolean unique(SQLException e) {
    return "23505".equals(e.getSQLState());
  }

  private DataAccessException data(SQLException e) {
    return new DataAccessException("Error de persistencia: " + e.getMessage(), e);
  }

  private void log(String m, String p) {
    System.out.printf("[HTTP] %s %s%n", m, p);
  }

  private void rollback(Connection c) {
    if (c != null)
      try {
        c.rollback();
      } catch (SQLException ignored) {
      }
  }

  private void close(Connection c) {
    if (c != null)
      try {
        c.close();
      } catch (SQLException ignored) {
      }
  }
}
