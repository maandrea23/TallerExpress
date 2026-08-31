package com.tallerexpress.dao;

import java.sql.Connection;
import java.util.List;

public interface CrudDao<T> {
  T create(Connection c, T value);

  void update(Connection c, T value);

  List<T> findAll(Connection c);
}
