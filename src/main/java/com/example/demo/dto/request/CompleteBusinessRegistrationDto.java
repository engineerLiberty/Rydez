package com.example.demo.dto.request;

import com.example.demo.enums.PaymentInterval;
import com.example.demo.enums.PaymentType;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;

@Data
public class CompleteBusinessRegistrationDto {


    private String companyName;

    @NotBlank(message = "Phone Number cannot be blank")
    private String phoneNumber;

    @NotBlank(message = "Address cannot be blank")
    private String address;

    @NotBlank(message = "State cannot be blank")
    private String state;

    @Enumerated(EnumType.STRING)
    private PaymentInterval paymentInterval;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;

}
