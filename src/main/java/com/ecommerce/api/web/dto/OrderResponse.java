package com.ecommerce.api.web.dto;

import com.ecommerce.api.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String customerEmail,
        List<OrderLineResponse> lines,
        BigDecimal totalAmount,
        OrderStatus status,
        Instant createdAt
) {

    public record OrderLineResponse(
            Long productId,
            String productName,
            int quantity,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {
    }
}
