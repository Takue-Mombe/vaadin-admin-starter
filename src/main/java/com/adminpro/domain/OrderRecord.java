package com.adminpro.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "order_record")
public class OrderRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Order number is required")
    @Size(max = 30)
    @Column(nullable = false, unique = true, length = 30)
    private String orderNumber;

    @NotBlank(message = "Customer is required")
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String customer;

    @NotBlank(message = "Plan is required")
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String planName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", message = "Amount cannot be negative")
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @NotBlank(message = "Status is required")
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @NotNull(message = "Date is required")
    @Column(nullable = false)
    private LocalDate orderedOn = LocalDate.now();

    public OrderRecord() {
    }

    public OrderRecord(String orderNumber,
                       String customer,
                       String planName,
                       BigDecimal amount,
                       String status,
                       LocalDate orderedOn) {
        this.orderNumber = orderNumber;
        this.customer = customer;
        this.planName = planName;
        this.amount = amount;
        this.status = status;
        this.orderedOn = orderedOn;
    }

    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getOrderedOn() {
        return orderedOn;
    }

    public void setOrderedOn(LocalDate orderedOn) {
        this.orderedOn = orderedOn;
    }
}
