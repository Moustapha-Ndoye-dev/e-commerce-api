package com.ecommerce.api.web.mapper;

import com.ecommerce.api.domain.Order;
import com.ecommerce.api.domain.OrderLine;
import com.ecommerce.api.domain.Product;
import com.ecommerce.api.web.dto.OrderResponse;
import com.ecommerce.api.web.dto.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                product.getCreatedAt()
        );
    }
}
