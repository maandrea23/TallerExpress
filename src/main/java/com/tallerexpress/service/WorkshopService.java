package com.tallerexpress.service;

import com.tallerexpress.config.Database;
import com.tallerexpress.dao.*;
import com.tallerexpress.dao.impl.*;
import com.tallerexpress.exception.*;
import com.tallerexpress.model.*;
import com.tallerexpress.service.user.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

/** Reglas de negocio para clientes, vehículos, usuarios y órdenes. */
public class WorkshopService {
  private final UserDao users = new JdbcUserDao();
  private final ClientDao clients = new JdbcClientDao();
  private final VehicleDao vehicles = new JdbcVehicleDao();
  private final ServiceOrderDao orders = new JdbcServiceOrderDao();
  private final UserCreator userCreator = new DefaultPropertiesUserCreator(new BaseUserCreator());

  public User login(String username, String password) {
    log("POST", "/login");
    try (Connection connection = Database.getConnection()) {
      return users.findActiveByCredentials(connection, username, password)
          .orElseThrow(() -> new BusinessException("Credenciales incorrectas o usuario inactivo"));
    } catch (SQLException e) { throw connectionError(e); }
  }

  public User createUser(String username, String password, String name) {
    if (blank(username) || blank(password) || blank(name))
      throw new BusinessException("Todos los datos del usuario son obligatorios");
    User user = userCreator.create(new User(null, username.trim(), password, name.trim(), null, null, null));
    log("POST", "/usuarios");
    try (Connection connection = Database.getConnection()) {
      return users.create(connection, user);
    } catch (SQLException e) { throw connectionError(e); }
    catch (DataAccessException e) {
      if (unique(e)) throw new BusinessException("El nombre de usuario ya existe");
      throw e;
    }
  }

  public List<User> users() {
    try (Connection connection = Database.getConnection()) {
      log("GET", "/usuarios"); return users.findAll(connection);
    } catch (SQLException e) { throw connectionError(e); }
  }

  public void toggleUser(long id) {
    try (Connection connection = Database.getConnection()) {
      if (!users.toggleStatus(connection, id)) throw new BusinessException("Usuario inexistente");
      log("PATCH", "/usuarios/" + id);
    } catch (SQLException e) { throw connectionError(e); }
  }

  public void deleteUser(long id) {
    try (Connection connection = Database.getConnection()) {
      if (!users.delete(connection, id)) throw new BusinessException("Usuario inexistente");
      log("DELETE", "/usuarios/" + id);
    } catch (SQLException e) { throw connectionError(e); }
  }

  public Client createClient(String document, String name, String phone, String email) {
    if (blank(document) || blank(name)) throw new BusinessException("Documento y nombre son obligatorios");
    Client client = new Client(null, document.trim(), name.trim(), phone, email, true, LocalDateTime.now());
    try (Connection connection = Database.getConnection()) {
      Client created = clients.create(connection, client); log("POST", "/clientes"); return created;
    } catch (SQLException e) { throw connectionError(e); }
    catch (DataAccessException e) {
      if (unique(e)) throw new BusinessException("El documento ya está registrado");
      throw e;
    }
  }

  public List<Client> clients() {
    try (Connection connection = Database.getConnection()) {
      log("GET", "/clientes"); return clients.findAll(connection);
    } catch (SQLException e) { throw connectionError(e); }
  }

  public Vehicle createVehicle(long clientId, String plate, String brand, String model, int year) {
    if (blank(plate) || blank(brand) || blank(model))
      throw new BusinessException("Placa, marca y modelo son obligatorios");
    try (Connection connection = Database.getConnection()) {
      if (!clients.isActive(connection, clientId))
        throw new BusinessException("El cliente no existe o está inactivo");
      Vehicle vehicle = new Vehicle(null, clientId, plate.trim().toUpperCase(), brand, model, year, LocalDateTime.now());
      Vehicle created = vehicles.create(connection, vehicle); log("POST", "/vehiculos"); return created;
    } catch (SQLException e) { throw connectionError(e); }
    catch (DataAccessException e) {
      if (unique(e)) throw new BusinessException("La placa ya está registrada");
      throw e;
    }
  }

  public List<Vehicle> vehicles(Long clientId) {
    try (Connection connection = Database.getConnection()) {
      log("GET", "/vehiculos"); return vehicles.findAll(connection, clientId);
    } catch (SQLException e) { throw connectionError(e); }
  }

  public long createOrder(long clientId, long vehicleId, String mechanic, String problem,
      String diagnosis, List<OrderPart> usedParts) {
    if (blank(mechanic) || blank(problem))
      throw new BusinessException("Mecánico y descripción del problema son obligatorios");
    Connection connection = null;
    try {
      connection = Database.getConnection();
      connection.setAutoCommit(false);
      if (!vehicles.belongsToActiveClient(connection, vehicleId, clientId))
        throw new BusinessException("El vehículo no pertenece a un cliente activo");
      ServiceOrder order = new ServiceOrder(null, clientId, vehicleId, mechanic, LocalDateTime.now(),
          problem, diagnosis, "ABIERTA", null);
      long id = orders.create(connection, order);
      for (OrderPart part : usedParts) addPart(connection, id, part);
      connection.commit(); log("POST", "/ordenes"); return id;
    } catch (Exception e) {
      rollback(connection);
      if (e instanceof BusinessException business) throw business;
      if (e instanceof DataAccessException access) throw access;
      if (e instanceof SQLException sql) throw connectionError(sql);
      throw new BusinessException("No se pudo registrar la orden: " + e.getMessage());
    } finally { close(connection); }
  }

  private void addPart(Connection connection, long orderId, OrderPart part) {
    if (part.quantity() <= 0) throw new BusinessException("La cantidad debe ser mayor que cero");
    ServiceOrderDao.PartAvailability availability = orders.lockPart(connection, part.partId())
        .orElseThrow(() -> new BusinessException("Repuesto inexistente o inactivo: " + part.partId()));
    if (!availability.active()) throw new BusinessException("Repuesto inexistente o inactivo: " + part.partId());
    if (availability.stockAvailable() < part.quantity())
      throw new BusinessException("Stock insuficiente para el repuesto " + part.partId());
    orders.addPart(connection, orderId, part.partId(), part.quantity(), availability.unitPrice());
    orders.decreaseStock(connection, part.partId(), part.quantity());
  }

  public BigDecimal finishOrder(long id) {
    Connection connection = null;
    try {
      connection = Database.getConnection(); connection.setAutoCommit(false);
      String status = orders.lockStatus(connection, id).orElse(null);
      if (!"ABIERTA".equals(status)) throw new BusinessException("La orden no existe o ya fue finalizada");
      BigDecimal total = orders.calculateTotal(connection, id);
      orders.finish(connection, id, total);
      connection.commit(); log("PATCH", "/ordenes/" + id); return total;
    } catch (Exception e) {
      rollback(connection);
      if (e instanceof BusinessException business) throw business;
      if (e instanceof DataAccessException access) throw access;
      if (e instanceof SQLException sql) throw connectionError(sql);
      throw new BusinessException("No se pudo finalizar la orden: " + e.getMessage());
    } finally { close(connection); }
  }

  public List<ServiceOrder> orders(Long vehicleId) {
    try (Connection connection = Database.getConnection()) { return orders.findAll(connection, vehicleId); }
    catch (SQLException e) { throw connectionError(e); }
  }

  private boolean blank(String value) { return value == null || value.isBlank(); }
  private boolean unique(Throwable error) {
    Throwable cause = error;
    while (cause != null) { if (cause instanceof SQLException sql && "23505".equals(sql.getSQLState())) return true; cause = cause.getCause(); }
    return false;
  }
  private DataAccessException connectionError(SQLException e) { return new DataAccessException("Error de conexión", e); }
  private void log(String method, String path) { System.out.printf("[HTTP] %s %s%n", method, path); }
  private void rollback(Connection c) { if (c != null) try { c.rollback(); } catch (SQLException ignored) { } }
  private void close(Connection c) { if (c != null) try { c.close(); } catch (SQLException ignored) { } }
}
