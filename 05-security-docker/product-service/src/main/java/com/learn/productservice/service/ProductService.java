package com.learn.productservice.service;

import com.learn.productservice.dto.ProductRequest;
import com.learn.productservice.dto.ProductResponse;
import com.learn.productservice.exception.ProductNotFoundException;
import com.learn.productservice.model.Product;
import com.learn.productservice.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public ProductResponse create(ProductRequest request) {
        Product product = new Product(request.name(), request.price(), request.stock());
        return ProductResponse.from(repository.save(product));
    }

    public List<ProductResponse> getAll() {
        return repository.findAll().stream().map(ProductResponse::from).toList();
    }

    public ProductResponse getById(Long id) {
        return ProductResponse.from(findOrThrow(id));
    }

    @Transactional
    public ProductResponse reduceStock(Long id, int quantity) {
        Product product = findOrThrow(id);
        product.reduceStock(quantity);   // dirty checking saves it at commit
        return ProductResponse.from(product);
    }

    private Product findOrThrow(Long id) {
        return repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
    }
}