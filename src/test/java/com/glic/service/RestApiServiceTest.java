package com.glic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glic.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RestApiServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ObjectMapper objectMapper;

    private RestApiService restApiService;

    @BeforeEach
    void setUp() {
        restApiService = new RestApiService(restTemplate, objectMapper, "http://localhost:8080");
    }

    @Test
    void createProduct_Success() throws Exception {
        // Arrange
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.0"));
        product.setQuantity(10);

        Product expectedResponse = new Product();
        expectedResponse.setId(1L);
        expectedResponse.setName("Test Product");
        expectedResponse.setPrice(new BigDecimal("100.0"));
        expectedResponse.setQuantity(10);

        String jsonResponse = "{\"id\":1,\"name\":\"Test Product\",\"price\":100.0,\"quantity\":10}";
        ResponseEntity<String> responseEntity = ResponseEntity.ok(jsonResponse);
        when(restTemplate.postForEntity(
                eq("http://localhost:8080/api/products"),
                any(HttpEntity.class),
                eq(String.class)))
                .thenReturn(responseEntity);
        when(objectMapper.readValue(eq(jsonResponse), eq(Product.class)))
                .thenReturn(expectedResponse);

        // Act
        Product result = restApiService.createProduct(product);

        // Assert
        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(expectedResponse.getName(), result.getName());
        assertEquals(expectedResponse.getPrice(), result.getPrice());
        assertEquals(expectedResponse.getQuantity(), result.getQuantity());
        verify(restTemplate).postForEntity(
                eq("http://localhost:8080/api/products"),
                any(HttpEntity.class),
                eq(String.class));
        verify(objectMapper).readValue(eq(jsonResponse), eq(Product.class));
    }

    @Test
    void createProduct_ThrowsException() {
        // Arrange
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new BigDecimal("100.0"));
        product.setQuantity(10);

        when(restTemplate.postForEntity(
                any(),
                any(),
                any()))
                .thenThrow(new RuntimeException("API Error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> restApiService.createProduct(product));
        verify(restTemplate).postForEntity(
                eq("http://localhost:8080/api/products"),
                any(HttpEntity.class),
                eq(String.class));
    }

    @Test
    void createProduct_WithNullProduct() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> restApiService.createProduct(null));
        verify(restTemplate, never()).postForEntity(
                any(),
                any(),
                any());
    }
} 