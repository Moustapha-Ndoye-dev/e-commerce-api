package com.ecommerce.api.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerEmail;

    @ElementCollection
    @CollectionTable(name = "order_lines", joinColumns = @JoinColumn(name = "order_id"))
    private List<OrderLine> lines = new ArrayList<>();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Order() {
    }

    public Order(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public List<OrderLine> getLines() {
        return lines;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void addLine(OrderLine line) {
        lines.add(line);
        totalAmount = totalAmount.add(line.getSubtotal());
    }

    public void confirm() {
        if (status != OrderStatus.PENDING) {
            throw new IllegalStateException("Only pending orders can be confirmed");
        }
        status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        if (status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order is already cancelled");
        }
        status = OrderStatus.CANCELLED;
    }
}
