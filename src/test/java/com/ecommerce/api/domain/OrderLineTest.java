package com.ecommerce.api.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OrderLine - tests unitaires domaine")
class OrderLineTest {

    @Test
    @DisplayName("getSubtotal calcule quantité × prix unitaire")
    void getSubtotal_multipliesQuantityByUnitPrice() {
        OrderLine line = new OrderLine(1L, "Keyboard", 3, new BigDecimal("25.50"));

        assertThat(line.getSubtotal()).isEqualByComparingTo("76.50");
        assertThat(line.getProductId()).isEqualTo(1L);
        assertThat(line.getProductName()).isEqualTo("Keyboard");
        assertThat(line.getQuantity()).isEqualTo(3);
        assertThat(line.getUnitPrice()).isEqualByComparingTo("25.50");
    }
}
