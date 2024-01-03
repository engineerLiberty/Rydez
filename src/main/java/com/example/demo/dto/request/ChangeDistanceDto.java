package com.example.demo.dto.request;

import lombok.Data;

@Data
public class ChangeDistanceDto {

    private Double price;
    private Double newDistanceStart;
    private Double newDistanceEnd;
}
