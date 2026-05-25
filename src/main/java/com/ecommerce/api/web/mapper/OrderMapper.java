package com.ecommerce.api.web.mapper;

import com.ecommerce.api.domain.Order;
import com.ecommerce.api.domain.OrderLine;
import com.ecommerce.api.web.dto.OrderResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerEmail(),
                order.getLines().stream().map(this::toLine).toList(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }

    private OrderResponse.OrderLineResponse toLine(OrderLine line) {
        return new OrderResponse.OrderLineResponse(
                line.getProductId(),
                line.getProductName(),
                line.getQuantity(),
                line.getUnitPrice(),
                line.getSubtotal()
        );
    }
}
