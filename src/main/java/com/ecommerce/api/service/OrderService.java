package com.ecommerce.api.service;

import com.ecommerce.api.domain.Order;
import com.ecommerce.api.domain.OrderLine;
import com.ecommerce.api.domain.Product;
import com.ecommerce.api.exception.BusinessException;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.OrderRepository;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.web.dto.CreateOrderRequest;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    public List<Order> findByCustomerEmail(String email) {
        return orderRepository.findByCustomerEmailOrderByCreatedAtDesc(email);
    }

    @Transactional
    public Order create(CreateOrderRequest request) {
        Order order = new Order(request.customerEmail());

        for (CreateOrderRequest.OrderItemRequest item : request.items()) {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.productId()));

            try {
                product.decreaseStock(item.quantity());
            } catch (IllegalStateException ex) {
                throw new BusinessException(ex.getMessage());
            }

            order.addLine(new OrderLine(
                    product.getId(),
                    product.getName(),
                    item.quantity(),
                    product.getPrice()
            ));
        }

        order.confirm();
        return orderRepository.save(order);
    }

    @Transactional
    public Order cancel(Long id) {
        Order order = findById(id);
        order.cancel();
        return order;
    }
}
