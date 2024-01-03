package com.example.demo.dto.response;

import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentType;
import lombok.Builder;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
@Data
@Builder
public class AllOrdersDetailsResponseDto {

    private Long id;
    private String referenceNumber;
    private String companyName;
    private String pickUpAddress;
    private String deliveryAddress;
    private String receiverName;
    private String receiverPhoneNumber;
    private Boolean thirdPartyPickUp;
    private String thirdPartyName;
    private String thirdPartyPhoneNumber;
    private String thirdPartyAddress;
    private String itemType;
    private int itemQuantity;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
    private double price;
    private PaymentType paymentType;
    private String reasonForOrderCancellation;
    private Long dispatchAdminNumber;
    private String customerFirstName;
    private String customerLastName;
    private String email;
    private Long clientCode;
    private double distance;
    private double unitPrice;
    private String bikeNumber;
    private String riderName;
    private Long riderId;
    private String riderPhoneNumber;

}
