package com.tallerexpress.dao.impl;
import com.tallerexpress.dao.UserDao;
import com.tallerexpress.exception.DataAccessException;
import com.tallerexpress.model.User;
import java.sql.*;
import java.util.*;

public final class JdbcUserDao implements UserDao {
  public Optional<User> findActiveByCredentials(Connection c, String username, String password) {
    try (PreparedStatement q = c.prepareStatement(
        "SELECT * FROM users WHERE username=? AND password=? AND status='ACTIVO'")) {
      q.setString(1, username); q.setString(2, password);
      try (ResultSet r = q.executeQuery()) { return r.next() ? Optional.of(map(r)) : Optional.empty(); }
    } catch (SQLException e) { throw error("Error consultando el usuario", e); }
  }
  public User create(Connection c, User u) {
    try (PreparedStatement q = c.prepareStatement(
        "INSERT INTO users(username,password,full_name,role,status,created_at) VALUES(?,?,?,?,?,?)",
        Statement.RETURN_GENERATED_KEYS)) {
      q.setString(1, u.username()); q.setString(2, u.password()); q.setString(3, u.fullName());
      q.setString(4, u.role()); q.setString(5, u.status()); q.setTimestamp(6, Timestamp.valueOf(u.createdAt()));
      q.executeUpdate();
      try (ResultSet r = q.getGeneratedKeys()) { r.next(); return new User(r.getLong(1), u.username(),
          u.password(), u.fullName(), u.role(), u.status(), u.createdAt()); }
    } catch (SQLException e) { throw error("Error creando el usuario", e); }
  }
  public List<User> findAll(Connection c) {
    List<User> out = new ArrayList<>();
    try (Statement q = c.createStatement(); ResultSet r = q.executeQuery("SELECT * FROM users ORDER BY id")) {
      while (r.next()) out.add(map(r)); return out;
    } catch (SQLException e) { throw error("Error consultando usuarios", e); }
  }
  public boolean toggleStatus(Connection c, long id) {
    try (PreparedStatement q = c.prepareStatement(
        "UPDATE users SET status=CASE status WHEN 'ACTIVO' THEN 'INACTIVO' ELSE 'ACTIVO' END WHERE id=?")) {
      q.setLong(1, id); return q.executeUpdate() > 0;
    } catch (SQLException e) { throw error("Error actualizando el usuario", e); }
  }
  public boolean delete(Connection c, long id) {
    try (PreparedStatement q = c.prepareStatement("DELETE FROM users WHERE id=?")) {
      q.setLong(1, id); return q.executeUpdate() > 0;
    } catch (SQLException e) { throw error("Error eliminando el usuario", e); }
  }
  private User map(ResultSet r) throws SQLException { return new User(r.getLong("id"), r.getString("username"),
      r.getString("password"), r.getString("full_name"), r.getString("role"), r.getString("status"),
      r.getTimestamp("created_at").toLocalDateTime()); }
  private DataAccessException error(String message, SQLException e) { return new DataAccessException(message, e); }
}
