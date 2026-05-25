package com.ecommerce.api.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ecommerce.api.domain.Order;
import com.ecommerce.api.domain.OrderLine;
import com.ecommerce.api.domain.OrderStatus;
import com.ecommerce.api.web.dto.OrderResponse;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OrderMapper - tests unitaires")
class OrderMapperTest {

    private final OrderMapper mapper = new OrderMapper();

    @Test
    @DisplayName("toResponse mappe la commande et ses lignes")
    void toResponse_mapsOrderAndLines() {
        Order order = new Order("buyer@example.com");
        order.addLine(new OrderLine(1L, "Mouse", 2, new BigDecimal("20.00")));
        order.confirm();

        OrderResponse response = mapper.toResponse(order);

        assertThat(response.customerEmail()).isEqualTo("buyer@example.com");
        assertThat(response.status()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(response.totalAmount()).isEqualByComparingTo("40.00");
        assertThat(response.lines()).hasSize(1);
        assertThat(response.lines().get(0).subtotal()).isEqualByComparingTo("40.00");
    }
}
