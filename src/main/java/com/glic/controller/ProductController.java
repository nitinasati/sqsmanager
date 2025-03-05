package com.glic.controller;

import com.glic.model.Product;
import com.glic.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for managing Product entities.
 * Provides endpoints for CRUD operations on products.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    /**
     * Logger instance for this controller.
     */
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    /**
     * Service for handling product-related business logic.
     */
    private final ProductService productService;

    /**
     * Constructor for ProductController.
     *
     * @param productService the service for product operations
     */
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Retrieves all products.
     *
     * @return a list of all products
     */
    @GetMapping
    public List<Product> getAllProducts() {
        logger.debug("Controller: Getting all products");
        List<Product> products = productService.getAllProducts();
        logger.debug("Controller: Found {} products", products.size());
        return products;
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id the ID of the product to find
     * @return ResponseEntity containing the product if found, or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        logger.debug("Controller: Getting product with id: {}", id);
        return productService.getProductById(id)
                .map(product -> {
                    logger.debug("Controller: Found product: {}", product);
                    return ResponseEntity.ok(product);
                })
                .orElseGet(() -> {
                    logger.debug("Controller: Product not found with id: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Creates a new product.
     *
     * @param product the product to create
     * @return ResponseEntity containing the created product
     */
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        logger.debug("Controller: Creating product: {}", product);
        Product createdProduct = productService.createProduct(product);
        logger.debug("Controller: Created product with id: {}", createdProduct.getId());
        return ResponseEntity.ok(createdProduct);
    }

    /**
     * Updates an existing product.
     *
     * @param id the ID of the product to update
     * @param product the updated product data
     * @return ResponseEntity containing the updated product, or 404 if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        logger.debug("Controller: Updating product with id: {}", id);
        try {
            Product updatedProduct = productService.updateProduct(id, product);
            logger.debug("Controller: Updated product with id: {}", updatedProduct.getId());
            return ResponseEntity.ok(updatedProduct);
        } catch (IllegalArgumentException e) {
            logger.error("Controller: Error updating product: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes a product.
     *
     * @param id the ID of the product to delete
     * @return ResponseEntity with no content if successful, or 404 if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        logger.debug("Controller: Deleting product with id: {}", id);
        try {
            productService.deleteProduct(id);
            logger.debug("Controller: Deleted product with id: {}", id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.error("Controller: Error deleting product: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
} 