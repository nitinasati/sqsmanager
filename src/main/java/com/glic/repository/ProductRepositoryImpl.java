package com.glic.repository;

import com.glic.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.List;

/**
 * Implementation of custom repository methods for Product entity.
 * This class provides additional database operations beyond the basic CRUD operations
 * defined in the ProductRepository interface.
 */
@Repository
public class ProductRepositoryImpl extends SimpleJpaRepository<Product, Long> implements ProductRepository {

    /**
     * Logger instance for this repository implementation.
     */
    private static final Logger logger = LoggerFactory.getLogger(ProductRepositoryImpl.class);

    /**
     * Entity manager for performing database operations.
     */
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Constructor for ProductRepositoryImpl.
     *
     * @param entityManager the entity manager
     */
    public ProductRepositoryImpl(EntityManager entityManager) {
        super(Product.class, entityManager);
        this.entityManager = entityManager;
    }

    /**
     * Saves a product entity to the database.
     *
     * @param entity the product entity to save
     * @return the saved product entity
     */
    @Override
    public <S extends Product> S save(S entity) {
        logger.debug("Repository: Saving product: {}", entity);
        S savedEntity = super.save(entity);
        logger.debug("Repository: Saved product with id: {}", savedEntity.getId());
        return savedEntity;
    }

    /**
     * Deletes a product entity from the database.
     *
     * @param entity the product entity to delete
     */
    @Override
    public void delete(Product entity) {
        logger.debug("Repository: Deleting product: {}", entity);
        super.delete(entity);
        logger.debug("Repository: Deleted product with id: {}", entity.getId());
    }

    /**
     * Finds a product by its name.
     *
     * @param name the name of the product to find
     * @return the product with the given name, or null if not found
     */
    @Override
    public Product findByName(String name) {
        logger.debug("Finding product with name: {}", name);
        TypedQuery<Product> query = entityManager.createQuery(
            "SELECT p FROM Product p WHERE p.name = :name", Product.class);
        query.setParameter("name", name);
        List<Product> results = query.getResultList();
        Product product = results.isEmpty() ? null : results.get(0);
        logger.debug("Found product: {}", product);
        return product;
    }

    /**
     * Finds all products with a price greater than the specified amount.
     *
     * @param price the minimum price threshold
     * @return a list of products with prices greater than the specified amount
     */
    public List<Product> findByPriceGreaterThan(BigDecimal price) {
        logger.debug("Finding products with price greater than: {}", price);
        TypedQuery<Product> query = entityManager.createQuery(
            "SELECT p FROM Product p WHERE p.price > :price", Product.class);
        query.setParameter("price", price);
        List<Product> results = query.getResultList();
        logger.debug("Found {} products with price greater than {}", results.size(), price);
        return results;
    }

    /**
     * Finds all products with a quantity less than the specified amount.
     *
     * @param quantity the maximum quantity threshold
     * @return a list of products with quantities less than the specified amount
     */
    public List<Product> findByQuantityLessThan(Integer quantity) {
        logger.debug("Finding products with quantity less than: {}", quantity);
        TypedQuery<Product> query = entityManager.createQuery(
            "SELECT p FROM Product p WHERE p.quantity < :quantity", Product.class);
        query.setParameter("quantity", quantity);
        List<Product> results = query.getResultList();
        logger.debug("Found {} products with quantity less than {}", results.size(), quantity);
        return results;
    }
} 