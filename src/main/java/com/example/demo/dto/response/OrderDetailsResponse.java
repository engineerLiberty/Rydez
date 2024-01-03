package com.example.demo.dto.response;

import com.example.demo.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderDetailsResponse {

    private Long id;
    private LocalDateTime createdAt;
    private String referenceNumber;
    private String companyName;
    private String pickUpAddress;
    private String phoneNumber;
    private String deliveryAddress;
    private String customerFirstName;
    private String customerLastName;
    private String receiverName;
    private String receiverPhoneNumber;
    private Boolean thirdPartyPickUp;
    private String thirdPartyName;
    private String thirdPartyPhoneNumber;
    private String thirdPartyAddress;
    private String itemType;
    private double distance;
    private Long riderId;
    private int itemQuantity;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
    private double price;

}
