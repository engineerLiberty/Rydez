package com.example.demo.repository;

import com.example.demo.dto.response.OrderDetailsResponse;
import com.example.demo.enums.OrderStatus;
import com.example.demo.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Orders, Long> {

    Optional<Orders> findByReferenceNumber(String referenceNumber);

    Optional<Orders> findByClientCodeAndReferenceNumber (Long clientCode, String referenceNumber);
    List<Orders> findByOrderStatus(OrderStatus orderStatus);
    List<Orders> findByRiderId (Long staffId);
    List<Orders> findAllByClientCodeAndCreatedAtBetween(Long clientCode, LocalDate startDate, LocalDate stopDate);
    List<Orders> findAllByCreatedAt (LocalDate localDate);
    List<Orders> findAllByCreatedAtBetween (LocalDate startDate, LocalDate stopDate);
    List<Orders> findAllByRiderIdAndCreatedAtBetween (Long riderId, LocalDate startDate, LocalDate endDate);

    @Query(value = "SELECT SUM(price) FROM orders WHERE client_code = :client_code " +
            "AND created_at BETWEEN cast(:startDate as Date) " + "AND cast(:endDate as Date)", nativeQuery = true)
    BigDecimal findSumOfOrderPrices (@Param("client_code") Long clientCode, @Param("startDate")
    LocalDateTime startDate, @Param("endDate") LocalDateTime stopDate);

    @Query(value = "SELECT * FROM orders WHERE client_code = :client_code", nativeQuery = true)
    ArrayList<OrderDetailsResponse> findAllOrdersOfAClient(@Param("client_code") Long clientCode);


    @Query(value = "SELECT * FROM orders WHERE client_code = :client_code AND created_at BETWEEN cast(:startDate as Date) AND cast(:endDate as Date)", nativeQuery = true)
    List<Orders> findOrderDetails(@Param("client_code") Long clientCode, @Param("startDate")LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    long count();

    List<Orders> findAllOrdersByClientCode (Long clientCode);

    @Query(value = "SELECT * FROM orders WHERE client_code = :client_code ORDER BY created_at", nativeQuery = true)
    List<Orders> findAllOrdersByCreatedAt (@Param("client_code") Long clientCode);

    @Query(value = "SELECT * FROM orders WHERE client_code = :client_code ORDER BY id", nativeQuery = true)
    List<Orders> findAllOrdersById (@Param("client_code") Long clientCode);

    Optional<Orders> findById (Long id);

//    Optional<OrderDetailsResponse> findAnOrderByReferenceNumber (String referenceNumber);

}