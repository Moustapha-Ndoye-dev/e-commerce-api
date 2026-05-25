package com.ecommerce.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.api.domain.Order;
import com.ecommerce.api.domain.OrderStatus;
import com.ecommerce.api.domain.Product;
import com.ecommerce.api.exception.BusinessException;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.OrderRepository;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.web.dto.CreateOrderRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService - tests unitaires")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("create commande valide avec décrémentation du stock")
    void create_validOrder_decreasesStockAndConfirms() {
        Product product = new Product("Laptop", "Desc", new BigDecimal("100.00"), 10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateOrderRequest request = new CreateOrderRequest(
                "client@example.com",
                List.of(new CreateOrderRequest.OrderItemRequest(1L, 2))
        );

        Order order = orderService.create(request);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("200.00");
        assertThat(product.getStock()).isEqualTo(8);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("create échoue si stock insuffisant")
    void create_insufficientStock_throwsBusinessException() {
        Product product = new Product("Laptop", "Desc", new BigDecimal("100.00"), 1);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        CreateOrderRequest request = new CreateOrderRequest(
                "client@example.com",
                List.of(new CreateOrderRequest.OrderItemRequest(1L, 5))
        );

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Insufficient stock");

        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("create échoue si produit introuvable")
    void create_unknownProduct_throwsNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        CreateOrderRequest request = new CreateOrderRequest(
                "client@example.com",
                List.of(new CreateOrderRequest.OrderItemRequest(99L, 1))
        );

        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("findById retourne la commande quand elle existe")
    void findById_whenExists_returnsOrder() {
        Order order = new Order("client@example.com");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.findById(1L);

        assertThat(result.getCustomerEmail()).isEqualTo("client@example.com");
    }

    @Test
    @DisplayName("findById lève une exception quand la commande n'existe pas")
    void findById_whenMissing_throwsNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("findByCustomerEmail retourne les commandes du client")
    void findByCustomerEmail_returnsOrders() {
        Order order = new Order("client@example.com");
        when(orderRepository.findByCustomerEmailOrderByCreatedAtDesc("client@example.com"))
                .thenReturn(List.of(order));

        List<Order> result = orderService.findByCustomerEmail("client@example.com");

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("cancel annule une commande existante")
    void cancel_existingOrder_setsCancelled() {
        Order order = new Order("client@example.com");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.cancel(1L);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("cancel échoue si commande introuvable")
    void cancel_unknownOrder_throwsNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.cancel(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
