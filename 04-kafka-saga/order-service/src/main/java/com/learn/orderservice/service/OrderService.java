package com.learn.orderservice.service;

import com.learn.orderservice.dto.OrderRequest;
import com.learn.orderservice.dto.OrderResponse;
import com.learn.orderservice.event.Json;
import com.learn.orderservice.event.OrderCreatedEvent;
import com.learn.orderservice.model.Order;
import com.learn.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final String ORDER_EVENTS = "order-events";

    private final OrderRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderService(OrderRepository repository, KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // Deliberately NOT @Transactional: save() commits on its own, so the order is in the
    // database before the event goes out and any reply can find it.
    public OrderResponse placeOrder(OrderRequest request) {
        Order order = repository.save(new Order(request.productId(), request.quantity()));

        OrderCreatedEvent event = new OrderCreatedEvent(
                order.getId(), order.getProductId(), order.getQuantity());

        // Key = order id, so all events of one order go to the same partition (kept in order)
        kafkaTemplate.send(ORDER_EVENTS, String.valueOf(order.getId()), Json.toJson(event));
        log.info("Order {} saved as PENDING, OrderCreatedEvent published", order.getId());

        return OrderResponse.from(order);
    }

    public List<OrderResponse> getAll() {
        return repository.findAll().stream().map(OrderResponse::from).toList();
    }

    public OrderResponse getById(Long id) {
        return repository.findById(id)
                .map(OrderResponse::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Order not found with id " + id));
    }
}
