package com.learn.orderservice.client;

import com.learn.orderservice.dto.ProductResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(RestClient productRestClient) {
        this.restClient = productRestClient;
    }

    public ProductResponse getProduct(Long id) {
        return restClient.get()
                .uri("/api/products/{id}", id)
                .retrieve()
                .onStatus(status -> status.value() == 404, (req, res) -> {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "Product " + id + " does not exist");
                })
                .body(ProductResponse.class);
    }

    public void reduceStock(Long id, int quantity) {
        restClient.put()
                .uri("/api/products/{id}/reduce-stock?quantity={q}", id, quantity)
                .retrieve()
                .onStatus(status -> status.value() == 409, (req, res) -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT,
                            "Insufficient stock for product " + id);
                })
                .toBodilessEntity();
    }
}