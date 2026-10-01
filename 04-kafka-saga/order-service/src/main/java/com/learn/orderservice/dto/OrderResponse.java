package com.learn.orderservice.dto;

import com.learn.orderservice.model.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(Long id, Long productId, String productName, Integer quantity,
                            BigDecimal totalPrice, String status, String failureReason,
                            LocalDateTime createdAt) {

    public static OrderResponse from(Order o) {
        return new OrderResponse(o.getId(), o.getProductId(), o.getProductName(),
                o.getQuantity(), o.getTotalPrice(), o.getStatus(), o.getFailureReason(),
                o.getCreatedAt());
    }
}
