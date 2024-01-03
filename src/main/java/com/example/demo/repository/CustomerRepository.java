package com.example.demo.repository;

import com.example.demo.enums.Role;
import com.example.demo.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    Boolean existsByEmail(String email);
    Optional<Customer> findByConfirmationToken(String token);
    List<Customer> findAll();
    Optional<Customer> findByClientCode(Long clientCode);
    long countByRole(Role role);
    Optional<Customer> findById (Long id);
}
