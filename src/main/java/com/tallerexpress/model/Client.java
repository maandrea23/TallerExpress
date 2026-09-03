package com.tallerexpress.model;

import java.time.LocalDateTime;

/** Cliente registrado en el taller. */
public final class Client {
  private final Long id;
  private final String document;
  private final String name;
  private final String phone;
  private final String email;
  private final boolean active;
  private final LocalDateTime createdAt;

  public Client(Long id, String document, String name, String phone, String email, boolean active,
      LocalDateTime createdAt) {
    this.id = id;
    this.document = document;
    this.name = name;
    this.phone = phone;
    this.email = email;
    this.active = active;
    this.createdAt = createdAt;
  }

  public Long id() { return id; }
  public String document() { return document; }
  public String name() { return name; }
  public String phone() { return phone; }
  public String email() { return email; }
  public boolean active() { return active; }
  public LocalDateTime createdAt() { return createdAt; }
}
