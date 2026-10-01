package com.learn.productservice.listener;

import com.learn.productservice.event.Json;
import com.learn.productservice.event.OrderCreatedEvent;
import com.learn.productservice.event.StockResultEvent;
import com.learn.productservice.service.StockReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final StockReservationService reservationService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderEventListener(StockReservationService reservationService,
                              KafkaTemplate<String, String> kafkaTemplate) {
        this.reservationService = reservationService;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "order-events")
    public void onOrderCreated(String message) {
        OrderCreatedEvent event = Json.fromJson(message, OrderCreatedEvent.class);

        // 1. Local transaction: reserve stock and record the outcome
        StockResultEvent result = reservationService.reserve(event);

        // 2. Only after the commit: announce the outcome
        kafkaTemplate.send("stock-events", String.valueOf(event.orderId()), Json.toJson(result));
        log.info("Order {}: stock {}{}", event.orderId(),
                result.reserved() ? "RESERVED" : "REJECTED",
                result.reserved() ? "" : " (" + result.reason() + ")");
    }
}
