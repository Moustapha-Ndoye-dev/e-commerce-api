package com.ecommerce.api.repository;

import com.ecommerce.api.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
