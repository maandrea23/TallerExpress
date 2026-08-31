package com.tallerexpress.model;

import java.time.LocalDateTime;

public record Client(
    Long id,
    String document,
    String name,
    String phone,
    String email,
    boolean active,
    LocalDateTime createdAt) {}
