package com.Mtng.Mtng;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * MtngApplicationTests – verifies the Spring context loads successfully.
 */
@SpringBootTest
@ActiveProfiles("test")
class MtngApplicationTests {

    @Test
    void contextLoads() {
        // Asserts the Spring application context starts without errors
    }
}
