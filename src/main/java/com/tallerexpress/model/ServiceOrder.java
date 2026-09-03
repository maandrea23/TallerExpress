package com.tallerexpress.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Orden de diagnóstico o reparación de un vehículo. */
public final class ServiceOrder {
  private final Long id;
  private final long clientId;
  private final long vehicleId;
  private final String mechanic;
  private final LocalDateTime entryDate;
  private final String problem;
  private final String diagnosis;
  private final String status;
  private final BigDecimal finalCost;

  public ServiceOrder(Long id, long clientId, long vehicleId, String mechanic,
      LocalDateTime entryDate, String problem, String diagnosis, String status,
      BigDecimal finalCost) {
    this.id = id;
    this.clientId = clientId;
    this.vehicleId = vehicleId;
    this.mechanic = mechanic;
    this.entryDate = entryDate;
    this.problem = problem;
    this.diagnosis = diagnosis;
    this.status = status;
    this.finalCost = finalCost;
  }

  public Long id() { return id; }
  public long clientId() { return clientId; }
  public long vehicleId() { return vehicleId; }
  public String mechanic() { return mechanic; }
  public LocalDateTime entryDate() { return entryDate; }
  public String problem() { return problem; }
  public String diagnosis() { return diagnosis; }
  public String status() { return status; }
  public BigDecimal finalCost() { return finalCost; }
}
