package com.tallerexpress.dao;
import com.tallerexpress.model.Client;
import java.sql.Connection;
import java.util.List;
public interface ClientDao {
  Client create(Connection connection, Client client);
  List<Client> findAll(Connection connection);
  boolean isActive(Connection connection, long id);
}
