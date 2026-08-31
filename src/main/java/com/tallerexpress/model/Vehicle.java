package com.tallerexpress.model;

import java.time.LocalDateTime;

public record Vehicle(
    Long id,
    long clientId,
    String plate,
    String brand,
    String model,
    int year,
    LocalDateTime createdAt) {}
