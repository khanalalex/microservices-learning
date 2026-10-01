package com.learn.productservice.repository;

import com.learn.productservice.model.StockReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockReservationRepository extends JpaRepository<StockReservation, Long> { }
