package com.example;

import java.util.UUID;

record OrderItem(
        UUID id,
        UUID orderId,
        UUID productId,
        Integer quantity
) {
}
