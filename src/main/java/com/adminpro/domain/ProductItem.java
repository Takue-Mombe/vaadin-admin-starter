package com.adminpro.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(name = "product_item")
public class ProductItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String name;

    @NotBlank(message = "SKU is required")
    @Size(max = 40)
    @Column(nullable = false, unique = true, length = 40)
    private String sku;

    @NotBlank(message = "Category is required")
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String category;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", message = "Price cannot be negative")
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    @Column(nullable = false)
    private Integer stock = 0;

    @NotBlank(message = "Status is required")
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String status = "ACTIVE";

    public ProductItem() {
    }

    public ProductItem(String name,
                       String sku,
                       String category,
                       BigDecimal price,
                       Integer stock,
                       String status) {
        this.name = name;
        this.sku = sku;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
