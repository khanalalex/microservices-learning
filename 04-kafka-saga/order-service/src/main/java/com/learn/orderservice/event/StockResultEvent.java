package com.learn.orderservice.event;

import java.math.BigDecimal;

// reserved = true  -> stock was taken, productName and totalPrice are filled in
// reserved = false -> stock was not taken, reason explains why
public record StockResultEvent(Long orderId, boolean reserved, String productName,
                               BigDecimal totalPrice, String reason) { }
