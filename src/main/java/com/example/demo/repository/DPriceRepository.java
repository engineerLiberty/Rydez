package com.example.demo.repository;

import com.example.demo.dto.response.DistancePriceResponse;
import com.example.demo.model.DPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface DPriceRepository extends JpaRepository<DPrice, Long> {

    List<DPrice> findAll();
}
