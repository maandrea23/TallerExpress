package com.tallerexpress.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Repuesto disponible en el inventario del taller. */
public final class Part {
  private final Long id;
  private final String code;
  private final String name;
  private final String category;
  private final String supplier;
  private final int stockTotal;
  private final int stockAvailable;
  private final BigDecimal unitPrice;
  private final boolean active;
  private final LocalDateTime createdAt;

  public Part(Long id, String code, String name, String category, String supplier, int stockTotal,
      int stockAvailable, BigDecimal unitPrice, boolean active, LocalDateTime createdAt) {
    this.id = id;
    this.code = code;
    this.name = name;
    this.category = category;
    this.supplier = supplier;
    this.stockTotal = stockTotal;
    this.stockAvailable = stockAvailable;
    this.unitPrice = unitPrice;
    this.active = active;
    this.createdAt = createdAt;
  }

  public Long id() { return id; }
  public String code() { return code; }
  public String name() { return name; }
  public String category() { return category; }
  public String supplier() { return supplier; }
  public int stockTotal() { return stockTotal; }
  public int stockAvailable() { return stockAvailable; }
  public BigDecimal unitPrice() { return unitPrice; }
  public boolean active() { return active; }
  public LocalDateTime createdAt() { return createdAt; }
}
