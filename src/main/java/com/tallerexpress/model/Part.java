package com.tallerexpress.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record Part(
    Long id,
    String code,
    String name,
    String category,
    String supplier,
    int stockTotal,
    int stockAvailable,
    BigDecimal unitPrice,
    boolean active,
    LocalDateTime createdAt) {}
