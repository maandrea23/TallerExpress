package com.tallerexpress.dao;
import com.tallerexpress.model.User;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
public interface UserDao {
  Optional<User> findActiveByCredentials(Connection connection, String username, String password);
  User create(Connection connection, User user);
  List<User> findAll(Connection connection);
  boolean toggleStatus(Connection connection, long id);
  boolean delete(Connection connection, long id);
}
