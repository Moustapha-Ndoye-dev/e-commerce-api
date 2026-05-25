package com.ecommerce.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ecommerce.api.domain.Product;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.web.dto.CreateProductRequest;
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
@DisplayName("ProductService - tests unitaires")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("findAll retourne la liste des produits")
    void findAll_returnsProducts() {
        Product laptop = new Product("Laptop", "Desc", new BigDecimal("999.99"), 10);
        when(productRepository.findAll()).thenReturn(List.of(laptop));

        List<Product> result = productService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Laptop");
    }

    @Test
    @DisplayName("findById retourne le produit quand il existe")
    void findById_whenExists_returnsProduct() {
        Product product = new Product("Mouse", "Desc", new BigDecimal("29.99"), 5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productService.findById(1L);

        assertThat(result.getName()).isEqualTo("Mouse");
    }

    @Test
    @DisplayName("findById lève une exception quand le produit n'existe pas")
    void findById_whenMissing_throwsNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("create persiste un nouveau produit")
    void create_savesProduct() {
        CreateProductRequest request = new CreateProductRequest(
                "Keyboard", "Mechanical", new BigDecimal("79.99"), 20
        );
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product result = productService.create(request);

        assertThat(result.getName()).isEqualTo("Keyboard");
        assertThat(result.getStock()).isEqualTo(20);
        verify(productRepository).save(any(Product.class));
    }
}
