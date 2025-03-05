package com.glic.service;

import com.glic.model.Product;
import com.glic.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing Product entities.
 * Provides business logic and transaction management for product operations.
 */
@Service
public class ProductService {

    /**
     * Logger instance for this service.
     */
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    /**
     * Repository for performing database operations on products.
     */
    private final ProductRepository productRepository;

    /**
     * Constructor for ProductService.
     *
     * @param productRepository the repository for product operations
     */
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Retrieves all products from the database.
     *
     * @return a list of all products
     */
    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        logger.debug("Service: Getting all products");
        List<Product> products = productRepository.findAll();
        logger.debug("Service: Found {} products", products.size());
        return products;
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id the ID of the product to find
     * @return an Optional containing the product if found, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        logger.debug("Service: Getting product with id: {}", id);
        Optional<Product> product = productRepository.findById(id);
        logger.debug("Service: Found product: {}", product);
        return product;
    }

    /**
     * Creates a new product in the database.
     *
     * @param product the product to create
     * @return the created product
     */
    @Transactional
    public Product createProduct(Product product) {
        logger.debug("Service: Creating product: {}", product);
        Product savedProduct = productRepository.save(product);
        logger.debug("Service: Created product with id: {}", savedProduct.getId());
        return savedProduct;
    }

    /**
     * Updates an existing product in the database.
     *
     * @param id the ID of the product to update
     * @param product the updated product data
     * @return the updated product
     */
    @Transactional
    public Product updateProduct(Long id, Product product) {
        logger.debug("Service: Updating product with id: {}", id);
        if (!productRepository.existsById(id)) {
            logger.error("Service: Product with id {} not found", id);
            throw new IllegalArgumentException("Product not found with id: " + id);
        }
        product.setId(id);
        Product updatedProduct = productRepository.save(product);
        logger.debug("Service: Updated product with id: {}", updatedProduct.getId());
        return updatedProduct;
    }

    /**
     * Deletes a product from the database.
     *
     * @param id the ID of the product to delete
     */
    @Transactional
    public void deleteProduct(Long id) {
        logger.debug("Service: Deleting product with id: {}", id);
        if (!productRepository.existsById(id)) {
            logger.error("Service: Product with id {} not found", id);
            throw new IllegalArgumentException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        logger.debug("Service: Deleted product with id: {}", id);
    }

    /**
     * Finds a product by its name.
     *
     * @param name the name of the product to find
     * @return the product with the given name, or null if not found
     */
    @Transactional(readOnly = true)
    public Product findByName(String name) {
        logger.debug("Service: Finding product with name: {}", name);
        Product product = productRepository.findByName(name);
        logger.debug("Service: Found product: {}", product);
        return product;
    }
} 