package com.learn.productservice.dto;

import com.learn.productservice.model.Product;
import java.math.BigDecimal;

public record ProductResponse(Long id, String name, BigDecimal price, Integer stock) {

    public static ProductResponse from(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getPrice(), p.getStock());
    }
}