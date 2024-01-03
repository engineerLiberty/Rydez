package com.example.demo.service;

import com.example.demo.dto.request.FeedbackDto;
import com.example.demo.dto.request.RiderFeedbackDto;
import com.example.demo.model.Feedback;
import org.springframework.data.domain.Page;

import java.time.LocalDate;

public interface FeedbackService {

    void provideFeedback (FeedbackDto feedbackDto);

    Page<Feedback> findAll(Integer pageNumber, Integer pageSize);

    Page<Feedback> viewPeriodicFeedback(Integer pageNumber, Integer pageSize, LocalDate startDate, LocalDate endDate);

    void rateARider(String referenceNumber, RiderFeedbackDto riderFeedbackDto);

}
