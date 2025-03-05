package com.glic.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * The Product class represents a product in the system.
 */
@Entity
public class Product {

    /**
     * The unique identifier of the product.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The name of the product.
     */
    @NotBlank(message = "Name is required")
    private String name;

    /**
     * The description of the product.
     */
    private String description;

    /**
     * The price of the product.
     */
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    /**
     * The quantity of the product in stock.
     */
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be non-negative")
    private Integer quantity;

    /**
     * Default constructor for Product class.
     * Required by JPA for entity instantiation.
     */
    public Product() {
    }

    /**
     * Gets the unique identifier of the product.
     *
     * @return the product ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the product.
     *
     * @param id the product ID to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the name of the product.
     *
     * @return the product name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the product.
     *
     * @param name the product name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of the product.
     *
     * @return the product description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the product.
     *
     * @param description the product description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the price of the product.
     *
     * @return the product price
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Sets the price of the product.
     *
     * @param price the product price to set
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * Gets the quantity of the product in stock.
     *
     * @return the product quantity
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity of the product in stock.
     *
     * @param quantity the product quantity to set
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
} 