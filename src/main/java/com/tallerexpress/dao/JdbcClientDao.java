package com.tallerexpress.dao;
import com.tallerexpress.exception.DataAccessException;
import com.tallerexpress.model.Client;
import java.sql.*;
import java.util.*;

public final class JdbcClientDao implements ClientDao {
  public Client create(Connection c, Client client) {
    try (PreparedStatement q = c.prepareStatement(
        "INSERT INTO clients(document,name,phone,email,active) VALUES(?,?,?,?,TRUE)", Statement.RETURN_GENERATED_KEYS)) {
      q.setString(1, client.document()); q.setString(2, client.name()); q.setString(3, client.phone()); q.setString(4, client.email());
      q.executeUpdate();
      try (ResultSet r = q.getGeneratedKeys()) { r.next(); return new Client(r.getLong(1), client.document(),
          client.name(), client.phone(), client.email(), true, client.createdAt()); }
    } catch (SQLException e) { throw error("Error creando el cliente", e); }
  }
  public List<Client> findAll(Connection c) {
    List<Client> out = new ArrayList<>();
    try (Statement q = c.createStatement(); ResultSet r = q.executeQuery("SELECT * FROM clients ORDER BY id")) {
      while (r.next()) out.add(new Client(r.getLong("id"), r.getString("document"), r.getString("name"),
          r.getString("phone"), r.getString("email"), r.getBoolean("active"),
          r.getTimestamp("created_at").toLocalDateTime())); return out;
    } catch (SQLException e) { throw error("Error consultando clientes", e); }
  }
  public boolean isActive(Connection c, long id) {
    try (PreparedStatement q = c.prepareStatement("SELECT active FROM clients WHERE id=?")) {
      q.setLong(1, id); try (ResultSet r = q.executeQuery()) { return r.next() && r.getBoolean(1); }
    } catch (SQLException e) { throw error("Error consultando el cliente", e); }
  }
  private DataAccessException error(String message, SQLException e) { return new DataAccessException(message, e); }
}
