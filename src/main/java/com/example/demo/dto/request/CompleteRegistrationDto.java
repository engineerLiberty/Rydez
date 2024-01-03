package com.example.demo.dto.request;

import com.example.demo.enums.Gender;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;

@Data
public class CompleteRegistrationDto {

    @NotBlank(message = "Phone Number cannot be blank")
    private String phoneNumber;

    @NotBlank(message = "Address cannot be blank")
    private String address;

    private String dob;

    @NotBlank(message = "State cannot be blank")
    private String state;

    @Enumerated(EnumType.STRING)
    private Gender gender;
}
