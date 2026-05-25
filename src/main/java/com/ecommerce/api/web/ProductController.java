package com.ecommerce.api.web;

import com.ecommerce.api.service.ProductService;
import com.ecommerce.api.web.dto.CreateProductRequest;
import com.ecommerce.api.web.dto.ProductResponse;
import com.ecommerce.api.web.mapper.ProductMapper;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    public ProductController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @GetMapping
    public List<ProductResponse> listProducts() {
        return productService.findAll().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id) {
        return productMapper.toResponse(productService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody CreateProductRequest request) {
        return productMapper.toResponse(productService.create(request));
    }
}
