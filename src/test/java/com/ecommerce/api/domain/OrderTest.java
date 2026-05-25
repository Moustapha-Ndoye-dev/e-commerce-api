package com.ecommerce.api.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Order - tests unitaires domaine")
class OrderTest {

    @Test
    @DisplayName("addLine calcule le total cumulé")
    void addLine_updatesTotalAmount() {
        Order order = new Order("client@example.com");
        order.addLine(new OrderLine(1L, "Product A", 2, new BigDecimal("10.00")));
        order.addLine(new OrderLine(2L, "Product B", 1, new BigDecimal("5.00")));

        assertThat(order.getLines()).hasSize(2);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("25.00");
    }

    @Test
    @DisplayName("confirm passe le statut à CONFIRMED")
    void confirm_pendingOrder_setsConfirmed() {
        Order order = new Order("client@example.com");

        order.confirm();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("confirm échoue si la commande n'est pas PENDING")
    void confirm_nonPendingOrder_throws() {
        Order order = new Order("client@example.com");
        order.confirm();

        assertThatThrownBy(order::confirm)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only pending orders can be confirmed");
    }

    @Test
    @DisplayName("cancel passe le statut à CANCELLED")
    void cancel_activeOrder_setsCancelled() {
        Order order = new Order("client@example.com");

        order.cancel();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancel échoue si la commande est déjà annulée")
    void cancel_alreadyCancelled_throws() {
        Order order = new Order("client@example.com");
        order.cancel();

        assertThatThrownBy(order::cancel)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already cancelled");
    }
}
