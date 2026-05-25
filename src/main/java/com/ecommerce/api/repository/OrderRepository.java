package com.ecommerce.api.repository;

import com.ecommerce.api.domain.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);
}
