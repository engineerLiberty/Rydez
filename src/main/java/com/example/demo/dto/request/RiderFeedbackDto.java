package com.example.demo.dto.request;

import com.example.demo.enums.RiderRating;
import com.example.demo.enums.RiderReport;
import lombok.Data;

@Data
public class RiderFeedbackDto {

    private Long staffId;
    private RiderRating riderRating;
    private RiderReport riderReport;
}
