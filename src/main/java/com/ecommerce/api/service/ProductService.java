package com.ecommerce.api.service;

import com.ecommerce.api.domain.Product;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.web.dto.CreateProductRequest;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
    }

    @Transactional
    public Product create(CreateProductRequest request) {
        Product product = new Product(
                request.name(),
                request.description(),
                request.price(),
                request.stock()
        );
        return productRepository.save(product);
    }
}
