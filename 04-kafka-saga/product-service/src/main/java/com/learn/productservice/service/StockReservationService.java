package com.learn.productservice.service;

import com.learn.productservice.event.OrderCreatedEvent;
import com.learn.productservice.event.StockResultEvent;
import com.learn.productservice.exception.InsufficientStockException;
import com.learn.productservice.model.Product;
import com.learn.productservice.model.StockReservation;
import com.learn.productservice.repository.ProductRepository;
import com.learn.productservice.repository.StockReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class StockReservationService {

    private final ProductRepository products;
    private final StockReservationRepository reservations;

    public StockReservationService(ProductRepository products, StockReservationRepository reservations) {
        this.products = products;
        this.reservations = reservations;
    }

    // Stock change and reservation record commit together, or not at all.
    @Transactional
    public StockResultEvent reserve(OrderCreatedEvent event) {
        // Duplicate delivery: return the stored outcome, do not touch stock again
        StockReservation existing = reservations.findById(event.orderId()).orElse(null);
        if (existing != null) {
            return toEvent(existing);
        }

        StockReservation outcome = decide(event);
        reservations.save(outcome);
        return toEvent(outcome);
    }

    private StockReservation decide(OrderCreatedEvent event) {
        Product product = products.findById(event.productId()).orElse(null);
        if (product == null) {
            return new StockReservation(event.orderId(), false,
                    "Product " + event.productId() + " does not exist", null, null);
        }
        try {
            product.reduceStock(event.quantity());   // saved by dirty checking at commit
        } catch (InsufficientStockException e) {
            return new StockReservation(event.orderId(), false, e.getMessage(), null, null);
        }
        BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(event.quantity()));
        return new StockReservation(event.orderId(), true, null, product.getName(), total);
    }

    private StockResultEvent toEvent(StockReservation r) {
        return new StockResultEvent(r.getOrderId(), r.isReserved(), r.getProductName(),
                r.getTotalPrice(), r.getReason());
    }
}
