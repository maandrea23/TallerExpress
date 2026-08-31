package com.tallerexpress.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServiceOrder(
    Long id,
    long clientId,
    long vehicleId,
    String mechanic,
    LocalDateTime entryDate,
    String problem,
    String diagnosis,
    String status,
    BigDecimal finalCost) {}
