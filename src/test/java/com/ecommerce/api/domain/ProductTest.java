package com.ecommerce.api.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Product - tests unitaires domaine")
class ProductTest {

    @Test
    @DisplayName("decreaseStock réduit le stock disponible")
    void decreaseStock_reducesQuantity() {
        Product product = new Product("Item", "Desc", new BigDecimal("10.00"), 5);

        product.decreaseStock(2);

        assertThat(product.getStock()).isEqualTo(3);
    }

    @Test
    @DisplayName("decreaseStock échoue si stock insuffisant")
    void decreaseStock_insufficientStock_throws() {
        Product product = new Product("Item", "Desc", new BigDecimal("10.00"), 1);

        assertThatThrownBy(() -> product.decreaseStock(3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("decreaseStock échoue si quantité négative ou nulle")
    void decreaseStock_invalidQuantity_throws() {
        Product product = new Product("Item", "Desc", new BigDecimal("10.00"), 5);

        assertThatThrownBy(() -> product.decreaseStock(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("positive");
    }
}
