package com.learn.orderservice.listener;

import com.learn.orderservice.event.Json;
import com.learn.orderservice.event.StockResultEvent;
import com.learn.orderservice.model.Order;
import com.learn.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StockResultListener {

    private static final Logger log = LoggerFactory.getLogger(StockResultListener.class);

    private final OrderRepository repository;

    public StockResultListener(OrderRepository repository) {
        this.repository = repository;
    }

    @KafkaListener(topics = "stock-events")
    @Transactional
    public void onStockResult(String message) {
        StockResultEvent event = Json.fromJson(message, StockResultEvent.class);

        Order order = repository.findById(event.orderId()).orElse(null);
        if (order == null) {
            log.warn("StockResultEvent for unknown order {}, ignoring", event.orderId());
            return;
        }
        // Idempotency: Kafka can deliver an event twice, so only act on PENDING orders
        if (!"PENDING".equals(order.getStatus())) {
            log.info("Order {} is already {}, ignoring duplicate event", order.getId(), order.getStatus());
            return;
        }

        if (event.reserved()) {
            order.confirm(event.productName(), event.totalPrice());
            log.info("Order {} CONFIRMED", order.getId());
        } else {
            order.cancel(event.reason());
            log.info("Order {} CANCELLED: {}", order.getId(), event.reason());
        }
        repository.save(order);
    }
}
