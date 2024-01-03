package com.example.demo.dto.response;

import com.example.demo.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GetAllStaffProfileDto {


    private Long id;
    private String firstName;
    private String lastName;
    private String dob;
    private String email;
    private String phoneNumber;
    private String address;
    private Long staffId;
    private String bankName;
    private String accountNumber;
    private LocalDateTime createdAt;
    private Role role;
    private float averageRating;
    private String nextOfKinFirstName;
    private String nextOfKinLastName;
    private String nextOfKinPhoneNumber;
    private String nextOfKinAddress;
    private String stateOfOrigin;

}
