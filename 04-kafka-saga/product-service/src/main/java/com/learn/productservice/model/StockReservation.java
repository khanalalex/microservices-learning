package com.learn.productservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

// One row per order that product-service has already decided on.
// The order id is the primary key, so one order can never be processed twice.
@Entity
@Table(name = "stock_reservations")
public class StockReservation {

    @Id
    private Long orderId;

    private boolean reserved;
    private String reason;
    private String productName;
    private BigDecimal totalPrice;

    protected StockReservation() { }

    public StockReservation(Long orderId, boolean reserved, String reason,
                            String productName, BigDecimal totalPrice) {
        this.orderId = orderId;
        this.reserved = reserved;
        this.reason = reason;
        this.productName = productName;
        this.totalPrice = totalPrice;
    }

    public Long getOrderId() { return orderId; }
    public boolean isReserved() { return reserved; }
    public String getReason() { return reason; }
    public String getProductName() { return productName; }
    public BigDecimal getTotalPrice() { return totalPrice; }
}
