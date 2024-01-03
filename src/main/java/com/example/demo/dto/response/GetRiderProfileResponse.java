package com.example.demo.dto.response;

import com.example.demo.enums.RiderStatus;
import com.example.demo.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GetRiderProfileResponse {

    private Long id;
    private LocalDateTime createdAt;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String address;
    private Long staffId;
    private RiderStatus riderStatus;
    private float averageRating;
    private int ridesCount;
    private String accountNumber;
    private String bankName;
    private String bikeNumber;
    private Role role;

}
