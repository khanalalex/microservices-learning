package com.learn.orderservice.event;

public record OrderCreatedEvent(Long orderId, Long productId, Integer quantity) { }
