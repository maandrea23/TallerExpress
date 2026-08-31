package com.tallerexpress.model;

import java.time.LocalDateTime;

public record User(
    Long id,
    String username,
    String password,
    String fullName,
    String role,
    String status,
    LocalDateTime createdAt) {}
