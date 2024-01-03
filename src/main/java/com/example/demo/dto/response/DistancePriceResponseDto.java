package com.example.demo.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DistancePriceResponseDto {

    private double distanceStart;
    private double distanceEnd;
    private double price;
}
