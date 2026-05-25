package com.ecommerce.api.web;

import com.ecommerce.api.service.OrderService;
import com.ecommerce.api.web.dto.CreateOrderRequest;
import com.ecommerce.api.web.dto.OrderResponse;
import com.ecommerce.api.web.mapper.OrderMapper;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    public OrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderMapper.toResponse(orderService.findById(id));
    }

    @GetMapping
    public List<OrderResponse> listOrders(@RequestParam String customerEmail) {
        return orderService.findByCustomerEmail(customerEmail).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderMapper.toResponse(orderService.create(request));
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancelOrder(@PathVariable Long id) {
        return orderMapper.toResponse(orderService.cancel(id));
    }
}
