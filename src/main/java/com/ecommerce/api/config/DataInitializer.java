package com.ecommerce.api.config;

import com.ecommerce.api.domain.Product;
import com.ecommerce.api.repository.ProductRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class DataInitializer {

    @Bean
    CommandLineRunner seedProducts(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() == 0) {
                productRepository.save(new Product("Laptop Pro", "15-inch developer laptop", new BigDecimal("1299.99"), 50));
                productRepository.save(new Product("Wireless Mouse", "Ergonomic Bluetooth mouse", new BigDecimal("49.99"), 200));
                productRepository.save(new Product("Mechanical Keyboard", "RGB mechanical keyboard", new BigDecimal("89.99"), 120));
                productRepository.save(new Product("USB-C Hub", "7-in-1 USB-C adapter", new BigDecimal("39.99"), 300));
                productRepository.save(new Product("Monitor 27\"", "4K IPS display", new BigDecimal("399.99"), 75));
            }
        };
    }
}
