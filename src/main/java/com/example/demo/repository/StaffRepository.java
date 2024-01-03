package com.example.demo.repository;

import com.example.demo.dto.response.OrderDetailsResponse;
import com.example.demo.enums.RiderStatus;
import com.example.demo.enums.Role;
import com.example.demo.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    Optional<Staff> findByEmail(String email);
    Boolean existsByEmail(String email);
    Optional<Staff> findByConfirmationToken(String token);
    Optional<Staff> findByStaffId(Long staffId);
    List<Staff> findByRiderStatus (RiderStatus riderStatus);
    List<Staff> findAll();
    List<Staff> findByRole (Role role);
    long countByRole(Role role);

}
