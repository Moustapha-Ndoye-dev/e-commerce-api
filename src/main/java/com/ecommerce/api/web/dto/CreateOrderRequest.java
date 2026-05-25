package com.ecommerce.api.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOrderRequest(
        @NotBlank @Email String customerEmail,
        @NotEmpty List<OrderItemRequest> items
) {

    public record OrderItemRequest(
            @NotNull Long productId,
            @NotNull @Min(1) Integer quantity
    ) {
    }
}
