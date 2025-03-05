package com.glic.repository;

import com.glic.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing Product entities.
 * Provides basic CRUD operations and custom query methods.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * Finds a product by its name.
     *
     * @param name the name of the product to find
     * @return the product with the given name, or null if not found
     */
    Product findByName(String name);
} 