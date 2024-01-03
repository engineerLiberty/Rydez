package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DistancePriceResponse {

    private String distanceRange;
    private Double price;
}
