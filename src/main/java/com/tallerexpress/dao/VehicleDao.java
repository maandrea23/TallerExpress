package com.tallerexpress.dao;
import com.tallerexpress.model.Vehicle;
import java.sql.Connection;
import java.util.List;
public interface VehicleDao {
  Vehicle create(Connection connection, Vehicle vehicle);
  List<Vehicle> findAll(Connection connection, Long clientId);
  boolean belongsToActiveClient(Connection connection, long vehicleId, long clientId);
}
