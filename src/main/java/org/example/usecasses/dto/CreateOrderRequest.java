package org.example.usecasses.dto;

import java.math.BigDecimal;

public record CreateOrderRequest(
        Integer clientId,
        String orderId,
        BigDecimal amount
) {}

