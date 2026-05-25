package com.ecommerce.api.web;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecommerce.api.web.dto.CreateProductRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("ProductController - tests d'intégration H2")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/products retourne une liste (base H2 vide au démarrage test)")
    void listProducts_returnsOk() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("POST puis GET /api/products/{id} avec persistance H2")
    void createAndGetProduct_persistsInH2() throws Exception {
        CreateProductRequest request = new CreateProductRequest(
                "Test Product", "Integration test", new BigDecimal("19.99"), 15
        );

        String response = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long id = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(15));
    }

    @Test
    @DisplayName("GET /api/products/{id} retourne 404 si absent en H2")
    void getProduct_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/products/{id}", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found: 99999"));
    }

    @Test
    @DisplayName("POST /api/products rejette une requête invalide")
    void createProduct_invalidRequest_returns400() throws Exception {
        String invalidJson = """
                {"name":"","price":-1,"stock":-5}
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details", hasSize(greaterThan(0))));
    }
}
