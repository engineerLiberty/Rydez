package com.example.demo.dto.response;

import com.example.demo.enums.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoggedInStaffProfileResponseDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String address;
    private Long staffId;
    private Role role;
}
