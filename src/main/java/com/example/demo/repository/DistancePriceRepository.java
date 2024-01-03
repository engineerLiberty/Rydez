package com.example.demo.repository;

import com.example.demo.model.DistancePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DistancePriceRepository extends JpaRepository<DistancePrice, Long> {
    @Query(value = "SELECT * FROM distance_price WHERE :distance BETWEEN distance_start AND distance_end", nativeQuery = true)
    DistancePrice findPriceByDistanceRange(@Param("distance") Double distance);
    DistancePrice findByPrice(Double price);
    List<DistancePrice> findAll();
}
