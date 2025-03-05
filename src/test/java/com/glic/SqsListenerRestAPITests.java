package com.glic;

import com.glic.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(classes = {SqsListenerRestAPI.class, TestConfig.class})
@TestPropertySource(locations = "classpath:application-test.properties")
class SqsListenerRestAPITests {

    @Test
    void contextLoads() {
        // This test will pass if the Spring context loads successfully
    }
} 