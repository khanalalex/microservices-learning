package com.learn.orderservice.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private String productName;      // filled in when the order is confirmed
    private Integer quantity;
    private BigDecimal totalPrice;   // filled in when the order is confirmed
    private String status;           // PENDING, CONFIRMED or CANCELLED
    private String failureReason;    // filled in when the order is cancelled
    private LocalDateTime createdAt;

    protected Order() { }

    public Order(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public void confirm(String productName, BigDecimal totalPrice) {
        this.productName = productName;
        this.totalPrice = totalPrice;
        this.status = "CONFIRMED";
    }

    // The saga's compensating action: the order is cancelled instead of confirmed
    public void cancel(String reason) {
        this.failureReason = reason;
        this.status = "CANCELLED";
    }

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public String getStatus() { return status; }
    public String getFailureReason() { return failureReason; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
