package com.learn.productservice.event;

public record OrderCreatedEvent(Long orderId, Long productId, Integer quantity) { }
