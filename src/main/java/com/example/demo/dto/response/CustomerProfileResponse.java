package com.example.demo.dto.response;

import com.example.demo.enums.CustomerType;
import com.example.demo.enums.Gender;
import com.example.demo.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerProfileResponse {

    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private Long clientCode;
    private Role role;
    private int orderCount;
    private CustomerType customerType;
    private Long id;
    private Gender gender;
    private boolean isActive;
    private Boolean discount;

}
