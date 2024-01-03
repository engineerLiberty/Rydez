package com.example.demo.dto.request;

import com.example.demo.enums.Gender;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;

@Data
public class RegisterRiderDto {


    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private String accountNumber;
    private String bankName;
    private String dob;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @NotBlank(message = "Next of kin first name cannot be blank")
    private String nextOfKinFirstName;
    @NotBlank(message = "Next of kin last name cannot be blank")
    private String nextOfKinLastName;
    @NotBlank(message = "Next of kin address cannot be blank")
    private String nextOfKinAddress;
    @NotBlank(message = "Next of kin phone number cannot be blank")
    private String nextOfKinPhoneNumber;
    @NotBlank(message = "State cannot be blank")
    private String stateOfOrigin;

}
