package com.example.demo.model;

import com.example.demo.enums.RiderRating;
import com.example.demo.enums.RiderReport;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Feedback extends Base{

    private String firstName;
    private String email;
    private String message;
    private Long staffId;
    @Enumerated(EnumType.STRING)
    private RiderReport riderReport;
    @Enumerated(EnumType.STRING)
    private RiderRating riderRating;
}
