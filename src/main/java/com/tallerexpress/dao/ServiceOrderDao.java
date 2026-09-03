package com.tallerexpress.dao;
import com.tallerexpress.model.ServiceOrder;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
public interface ServiceOrderDao {
  record PartAvailability(BigDecimal unitPrice, int stockAvailable, boolean active) {}
  long create(Connection connection, ServiceOrder order);
  Optional<PartAvailability> lockPart(Connection connection, long partId);
  void addPart(Connection connection, long orderId, long partId, int quantity, BigDecimal unitPrice);
  void decreaseStock(Connection connection, long partId, int quantity);
  Optional<String> lockStatus(Connection connection, long orderId);
  BigDecimal calculateTotal(Connection connection, long orderId);
  void finish(Connection connection, long orderId, BigDecimal total);
  List<ServiceOrder> findAll(Connection connection, Long vehicleId);
}
