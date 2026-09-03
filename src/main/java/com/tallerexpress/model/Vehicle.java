package com.tallerexpress.model;

import java.time.LocalDateTime;

/** Vehículo asociado a un cliente. */
public final class Vehicle {
  private final Long id;
  private final long clientId;
  private final String plate;
  private final String brand;
  private final String model;
  private final int year;
  private final LocalDateTime createdAt;

  public Vehicle(Long id, long clientId, String plate, String brand, String model, int year,
      LocalDateTime createdAt) {
    this.id = id;
    this.clientId = clientId;
    this.plate = plate;
    this.brand = brand;
    this.model = model;
    this.year = year;
    this.createdAt = createdAt;
  }

  public Long id() { return id; }
  public long clientId() { return clientId; }
  public String plate() { return plate; }
  public String brand() { return brand; }
  public String model() { return model; }
  public int year() { return year; }
  public LocalDateTime createdAt() { return createdAt; }
}
