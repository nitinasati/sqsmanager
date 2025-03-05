package com.glic.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.glic.model.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Service class responsible for handling REST API operations for Product entities.
 * This service provides methods to interact with a REST API endpoint for CRUD operations on products.
 */
@Slf4j
@Service
public class RestApiService {

    /**
     * The RestTemplate instance for making HTTP requests.
     */
    private final RestTemplate restTemplate;

    /**
     * The ObjectMapper instance for JSON serialization/deserialization.
     */
    private final ObjectMapper objectMapper;

    /**
     * The base URL of the REST API.
     */
    private final String baseUrl;

    /**
     * The endpoint path for product operations.
     */
    private static final String PRODUCTS_ENDPOINT = "/api/products";

    /**
     * Maximum number of retry attempts for failed API calls.
     */
    private static final int MAX_RETRY_ATTEMPTS = 3;

    /**
     * Delay in milliseconds between retry attempts.
     */
    private static final long RETRY_DELAY_MS = 1000;

    /**
     * Constructs a new RestApiService with the required dependencies.
     *
     * @param restTemplate The RestTemplate instance for making HTTP requests
     * @param objectMapper The ObjectMapper instance for JSON serialization/deserialization
     * @param baseUrl The base URL of the REST API
     */
    @Autowired
    public RestApiService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${api.base.url:http://localhost:8080}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
    }

    /**
     * Retrieves all products from the REST API.
     *
     * @return A list of all products, or an empty list if no products are found or an error occurs
     */
    public List<Product> getAllProducts() {
        try {
            ResponseEntity<List> response = restTemplate.getForEntity(
                    baseUrl + PRODUCTS_ENDPOINT,
                    List.class);
            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error fetching all products: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Retrieves a specific product by its ID from the REST API.
     *
     * @param id The ID of the product to retrieve
     * @return The product if found, null otherwise
     * @throws IllegalArgumentException if the ID is null
     */
    public Product getProductById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        try {
            ResponseEntity<Product> response = restTemplate.getForEntity(
                    baseUrl + PRODUCTS_ENDPOINT + "/" + id,
                    Product.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching product with ID {}: {}", id, e.getMessage());
            return null;
        }
    }

    /**
     * Creates a new product through the REST API.
     *
     * @param product The product to create
     * @return The created product with its assigned ID
     * @throws IllegalArgumentException if the product is null
     * @throws RuntimeException if the API call fails after all retry attempts
     */
    public Product createProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        String url = baseUrl + PRODUCTS_ENDPOINT;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            String jsonBody = objectMapper.writeValueAsString(product);
            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

            log.debug("Making POST request to {} with body: {}", url, jsonBody);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            log.debug("Received response: {}", response.getBody());

            return objectMapper.readValue(response.getBody(), Product.class);
        } catch (Exception e) {
            log.error("Error creating product: {}", e.getMessage());
            throw new RuntimeException("Failed to create product", e);
        }
    }

    /**
     * Updates an existing product through the REST API.
     *
     * @param id The ID of the product to update
     * @param product The updated product data
     * @return The updated product
     * @throws IllegalArgumentException if either the ID or product is null
     * @throws RuntimeException if the API call fails
     */
    public Product updateProduct(Long id, Product product) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Product> request = new HttpEntity<>(product, headers);
            
            ResponseEntity<Product> response = restTemplate.exchange(
                    baseUrl + PRODUCTS_ENDPOINT + "/" + id,
                    org.springframework.http.HttpMethod.PUT,
                    request,
                    Product.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error updating product with ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to update product", e);
        }
    }

    /**
     * Deletes a product through the REST API.
     *
     * @param id The ID of the product to delete
     * @throws IllegalArgumentException if the ID is null
     * @throws RuntimeException if the API call fails
     */
    public void deleteProduct(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        try {
            restTemplate.delete(baseUrl + PRODUCTS_ENDPOINT + "/" + id);
        } catch (Exception e) {
            log.error("Error deleting product with ID {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete product", e);
        }
    }

    /**
     * Makes a POST request to the REST API with retry logic.
     *
     * @param url the URL to send the request to
     * @param requestBody the request body as a Map
     * @return the response body as a String
     * @throws RuntimeException if the API call fails after all retry attempts
     */
    public String makePostRequest(String url, Map<String, Object> requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

            int attempts = 0;
            while (attempts < MAX_RETRY_ATTEMPTS) {
                try {
                    log.debug("Making POST request to {} with body: {}", url, jsonBody);
                    ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
                    log.debug("Received response: {}", response.getBody());
                    return response.getBody();
                } catch (Exception e) {
                    attempts++;
                    if (attempts == MAX_RETRY_ATTEMPTS) {
                        log.error("Failed to make POST request after {} attempts: {}", MAX_RETRY_ATTEMPTS, e.getMessage());
                        throw new RuntimeException("Failed to make POST request", e);
                    }
                    log.warn("Attempt {} failed, retrying in {} ms: {}", attempts, RETRY_DELAY_MS, e.getMessage());
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
            throw new RuntimeException("Failed to make POST request after " + MAX_RETRY_ATTEMPTS + " attempts");
        } catch (Exception e) {
            log.error("Error making POST request: {}", e.getMessage());
            throw new RuntimeException("Failed to make POST request", e);
        }
    }

    /**
     * Makes a POST request to the REST API with retry logic.
     *
     * @param url the URL to send the request to
     * @param requestBody the request body as a String
     * @return the response body as a String
     * @throws RuntimeException if the API call fails after all retry attempts
     */
    public String makePostRequest(String url, String requestBody) {
        return makePostRequest(url, Collections.singletonMap("body", requestBody));
    }
} 