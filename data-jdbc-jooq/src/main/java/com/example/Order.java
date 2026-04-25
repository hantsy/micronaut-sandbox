package com.example;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

record Order(
        UUID id,
        UUID customerId,
        List<OrderItem> items,
        BigDecimal amount,
        LocalDateTime createdAt
) {
}
