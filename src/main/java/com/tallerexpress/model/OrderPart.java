package com.tallerexpress.model;

/** Cantidad de un repuesto que se utilizará en una orden. */
public final class OrderPart {
  private final long partId;
  private final int quantity;

  public OrderPart(long partId, int quantity) {
    this.partId = partId;
    this.quantity = quantity;
  }

  public long partId() { return partId; }
  public int quantity() { return quantity; }
}
