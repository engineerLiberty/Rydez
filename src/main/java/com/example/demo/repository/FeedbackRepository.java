package com.example.demo.repository;

import com.example.demo.model.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Page<Feedback> findAll(Pageable pageable);
    Page<Feedback> findByCreatedAtBetween (Pageable pageable, LocalDate startDate, LocalDate endDate);
}
