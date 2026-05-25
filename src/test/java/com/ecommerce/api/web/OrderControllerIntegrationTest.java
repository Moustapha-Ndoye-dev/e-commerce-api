package com.ecommerce.api.web;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ecommerce.api.web.dto.CreateOrderRequest;
import com.ecommerce.api.web.dto.CreateProductRequest;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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
@DisplayName("OrderController - tests d'intégration H2")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long productId;

    @BeforeEach
    void seedProduct() throws Exception {
        CreateProductRequest product = new CreateProductRequest(
                "Order Test Product", "For order flow", new BigDecimal("50.00"), 100
        );

        String response = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        productId = objectMapper.readTree(response).get("id").asLong();
    }

    @Test
    @DisplayName("POST /api/orders crée une commande confirmée en H2")
    void createOrder_persistsOrder() throws Exception {
        CreateOrderRequest order = new CreateOrderRequest(
                "buyer@example.com",
                List.of(new CreateOrderRequest.OrderItemRequest(productId, 2))
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.totalAmount", is(100.0)))
                .andExpect(jsonPath("$.lines[0].quantity", is(2)));
    }

    @Test
    @DisplayName("GET /api/orders?customerEmail filtre par client")
    void listOrders_byEmail() throws Exception {
        CreateOrderRequest order = new CreateOrderRequest(
                "filter@example.com",
                List.of(new CreateOrderRequest.OrderItemRequest(productId, 1))
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/orders").param("customerEmail", "filter@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerEmail", is("filter@example.com")));
    }

    @Test
    @DisplayName("POST /api/orders échoue si stock insuffisant")
    void createOrder_insufficientStock_returns400() throws Exception {
        CreateOrderRequest order = new CreateOrderRequest(
                "buyer@example.com",
                List.of(new CreateOrderRequest.OrderItemRequest(productId, 500))
        );

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient stock for product: Order Test Product"));
    }

    @Test
    @DisplayName("GET /api/orders/{id} retourne une commande existante")
    void getOrder_byId() throws Exception {
        CreateOrderRequest order = new CreateOrderRequest(
                "buyer@example.com",
                List.of(new CreateOrderRequest.OrderItemRequest(productId, 1))
        );

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long orderId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(orderId.intValue())))
                .andExpect(jsonPath("$.customerEmail", is("buyer@example.com")));
    }

    @Test
    @DisplayName("POST /api/orders/{id}/cancel annule une commande")
    void cancelOrder_setsCancelled() throws Exception {
        CreateOrderRequest order = new CreateOrderRequest(
                "cancel@example.com",
                List.of(new CreateOrderRequest.OrderItemRequest(productId, 1))
        );

        String response = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long orderId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(post("/api/orders/{id}/cancel", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));
    }

    @Test
    @DisplayName("GET /api/orders/{id} retourne 404 si absent")
    void getOrder_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/orders/{id}", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order not found: 99999"));
    }

    @Test
    @DisplayName("POST /api/orders rejette une requête invalide")
    void createOrder_invalidRequest_returns400() throws Exception {
        String invalidJson = """
                {"customerEmail":"not-an-email","items":[]}
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
