package com.ecommerce.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("EcommerceApplication - test de contexte Spring")
class EcommerceApplicationTest {

    @Test
    @DisplayName("Le contexte Spring démarre avec le profil test et H2")
    void contextLoads() {
    }
}
