package com.example.demo.dto.request;

import lombok.Data;

@Data
public class DispatchOrderDto {
    private Long riderId;
    private Boolean assignExtraOrder;
}
