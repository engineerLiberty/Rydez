package com.example.demo.dto.request;

import lombok.Data;

import java.time.LocalDate;
@Data
public class WeeklyOrderSummaryDto {

    private Long clientCode;
    private LocalDate startDate;
    private LocalDate endDate;

}
