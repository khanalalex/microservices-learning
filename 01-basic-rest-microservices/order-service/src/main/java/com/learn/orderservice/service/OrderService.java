package com.learn.orderservice.service;

import com.learn.orderservice.client.ProductClient;
import com.learn.orderservice.dto.OrderRequest;
import com.learn.orderservice.dto.OrderResponse;
import com.learn.orderservice.dto.ProductResponse;
import com.learn.orderservice.model.Order;
import com.learn.orderservice.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository repository;
    private final ProductClient productClient;

    public OrderService(OrderRepository repository, ProductClient productClient) {
        this.repository = repository;
        this.productClient = productClient;
    }

    public OrderResponse placeOrder(OrderRequest request) {
        // 1. Remote call: get product details
        ProductResponse product = productClient.getProduct(request.productId());

        // 2. Remote call: reserve stock (fails with 409 if not enough)
        productClient.reduceStock(request.productId(), request.quantity());

        // 3. Local DB: save the order
        BigDecimal total = product.price().multiply(BigDecimal.valueOf(request.quantity()));
        Order order = new Order(product.id(), product.name(), request.quantity(), total);
        return OrderResponse.from(repository.save(order));
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