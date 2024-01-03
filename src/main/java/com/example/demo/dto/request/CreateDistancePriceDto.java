package com.example.demo.dto.request;

import lombok.Data;

@Data
public class CreateDistancePriceDto {

    private Double distanceStart;
    private Double distanceEnd;
    private Double price;
}
