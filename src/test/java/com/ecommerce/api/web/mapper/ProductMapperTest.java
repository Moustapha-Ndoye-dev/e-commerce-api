package com.ecommerce.api.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.ecommerce.api.domain.Product;
import com.ecommerce.api.web.dto.ProductResponse;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ProductMapper - tests unitaires")
class ProductMapperTest {

    private final ProductMapper mapper = new ProductMapper();

    @Test
    @DisplayName("toResponse mappe tous les champs du produit")
    void toResponse_mapsAllFields() {
        Product product = new Product("Monitor", "4K display", new BigDecimal("399.99"), 10);

        ProductResponse response = mapper.toResponse(product);

        assertThat(response.name()).isEqualTo("Monitor");
        assertThat(response.description()).isEqualTo("4K display");
        assertThat(response.price()).isEqualByComparingTo("399.99");
        assertThat(response.stock()).isEqualTo(10);
        assertThat(response.createdAt()).isNotNull();
    }
}
