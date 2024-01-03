package com.example.demo.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PeriodicBillDto {

    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
