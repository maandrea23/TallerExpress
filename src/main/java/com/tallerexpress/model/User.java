package com.tallerexpress.model;

import java.time.LocalDateTime;

/** Usuario autorizado para ingresar al sistema. */
public final class User {
  private final Long id;
  private final String username;
  private final String password;
  private final String fullName;
  private final String role;
  private final String status;
  private final LocalDateTime createdAt;

  public User(Long id, String username, String password, String fullName, String role, String status,
      LocalDateTime createdAt) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.fullName = fullName;
    this.role = role;
    this.status = status;
    this.createdAt = createdAt;
  }

  public Long id() { return id; }
  public String username() { return username; }
  public String password() { return password; }
  public String fullName() { return fullName; }
  public String role() { return role; }
  public String status() { return status; }
  public LocalDateTime createdAt() { return createdAt; }
}
