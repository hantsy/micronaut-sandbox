package com.example;

import java.math.BigDecimal;
import java.util.UUID;

record Product(
        UUID id,
        String name,
        BigDecimal price
) {
}
