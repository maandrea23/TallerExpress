package com.tallerexpress.dao;

import com.tallerexpress.model.Part;
import java.sql.Connection;
import java.util.List;

public interface PartDao extends CrudDao<Part> {
  boolean existsByCode(Connection c, String code, Long excludedId);

  List<Part> filter(Connection c, String category, String supplier);
}
