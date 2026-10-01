package com.learn.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderRequest(
        @NotNull Long productId,
        @NotNull @Min(1) Integer quantity) { }